package com.bike.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bike.common.cache.CommonCacheUtil;
import com.bike.common.constants.Constants;
import com.bike.common.exception.MaMaBikeException;
import com.bike.common.utils.QiniuFileUploadUtil;
import com.bike.common.utils.RandomNumberCode;
import com.bike.jms.SmsProcessor;
import com.bike.security.AESUtil;
import com.bike.security.Base64Util;
import com.bike.security.MD5Util;
import com.bike.security.RSAUtil;
import com.bike.user.dao.UserMapper;
import com.bike.user.entity.User;
import com.bike.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.jms.Destination;
import java.util.HashMap;
import java.util.Map;

@Slf4j//日志
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;//User用户操作

    @Autowired
    private CommonCacheUtil cacheUtil;//Redis缓存操作

    @Autowired
    private SmsProcessor smsProcessor;//ActiveMQ短信发送操作

    private static final String VERIFYCODE_PREFIX = "verify.code.";
    private static final String SMS_QUEUE = "sms.queue";

    /**
     * 1、登录业务
     */
    @Override
    //@Transactional
    public String login(String data, String key) throws MaMaBikeException {
        String decryptData = null;
        String token = null;
        try {
            //RSA解密AES的key,解码成字节码
            byte[] aesKey = RSAUtil.decryptByPrivateKey(Base64Util.decode(key));
            //AES的key解密AES加密数据
            decryptData = AESUtil.decrypt(data, new String(aesKey, "UTF-8"));//字节转字符后作为参数解密data
            if (decryptData == null) {
                throw new Exception();
            }

            //拿到提交json格式数据,开始验证逻辑
            JSONObject jsonObject = JSON.parseObject(decryptData);//string->json
            String mobile = jsonObject.getString("mobile");//电话
            String code = jsonObject.getString("code");//验证码
            String platform = jsonObject.getString("platform");//机器类型 Android/ios
            String channelId = jsonObject.getString("channelId");//推送频道编码 单个设备唯一

            if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code) || StringUtils.isBlank(platform) || StringUtils.isBlank(channelId)) {
                throw new Exception();
            }

            //去redis取验证码比较手机号码和验证码是否匹配,若匹配,说明是本人手机
            String verCode = cacheUtil.getCacheValue(VERIFYCODE_PREFIX + mobile);
            User user = null;
            if (code.equals(verCode)) {
                //1、检查用户是否存在
                user = userMapper.selectByMobile("17729842954");
                if (user == null) {
                    //2、用户不存在时,注册用户
                    user = new User();
                    user.setMobile(mobile);
                    user.setNickname("Kyrie Irving");
                    userMapper.insertSelective(user);
                }
            } else {
                throw new MaMaBikeException("手机号与验证码不匹配");
            }

            //生成token
            try {
                token = this.generateToken(user);
            } catch (Exception e) {
                throw new MaMaBikeException("fail.to.generate.token");
            }

            //redis存储信息
            UserElement ue = new UserElement();
            ue.setMobile(mobile);
            ue.setUserId(user.getId());
            ue.setToken(token);
            ue.setPlatform(platform);
            ue.setPushChannelId(channelId);
            cacheUtil.putTokenWhenLogin(ue);
        } catch (Exception e) {
            log.error("Fail to decrypt data", e);
            throw new MaMaBikeException("数据解析错误");//自定义异常
        }

        return token;
    }

    /**
     * 生成token
     */
    private String generateToken(User user) throws Exception {
        String source = user.getId() + ":" + user.getMobile() + System.currentTimeMillis();
        return MD5Util.getMD5(source);
    }

    /**
     * 2、修改用户昵称
     */
    //@Transactional
    @Override
    public void modifyNickName(User user) throws MaMaBikeException {
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 3、发送验证码
     */
    @Override
    public void sendVercode(String mobile, String ip) throws MaMaBikeException {
        //随机生成验证码
        String verCode = RandomNumberCode.verCode();
        //先存Redis,Redis缓存检查是否恶意请求,决定是否真的发送验证码
        int result = cacheUtil.cacheForVerificationCode(VERIFYCODE_PREFIX + mobile, verCode, "reg", 600, ip);
        if (result == 1) {
            log.info("当前验证码未过期，请稍后重试");
            throw new MaMaBikeException("当前验证码未过期，请稍后重试");
        } else if (result == 2) {
            log.info("超过当日验证码次数上线");
            throw new MaMaBikeException("超过当日验证码次数上限");
        } else if (result == 3) {
            log.info("超过当日验证码次数上限 {}", ip);
            throw new MaMaBikeException(ip + "超过当日验证码次数上限");
        }
        log.info("Sending verify code {} for phone {}", verCode, mobile);

        //校验通过,发送短信,验证码推送到队列(异步解耦,发送不管成功与否不影响后续程序运行)
        Destination destination = new ActiveMQQueue(SMS_QUEUE);
        //构造短信发送参数
        Map<String, String> smsParam = new HashMap<>();
        smsParam.put("mobile", mobile);
        smsParam.put("tplId", Constants.MDSMS_VERCODE_TPLID);
        smsParam.put("vercode", verCode);
        String message = JSON.toJSONString(smsParam);
        //smsProcessor.sendSmsToQueue(destination, message);//秒嘀平台发送短信接口
        smsProcessor.sendMessage(verCode, mobile);//企业平台发送短信接口
    }

    /**
     * 4、上传头像
     */
    //@Transactional
    @Override
    public String uploadHeadImg(MultipartFile file, long userId) throws MaMaBikeException {
        try {
            //获取user,得到原来的头像地址
            User user = userMapper.selectByPrimaryKey(userId);
            //调用七牛(生成七牛云上空间名称下的文件名)
            String imgUrlName = QiniuFileUploadUtil.uploadHeadImg(file);
            user.setHeadImg(imgUrlName);
            //更新用户头像URL
            userMapper.updateByPrimaryKeySelective(user);
            //线上文件链接
            //String file_url = Constants.QINIU_HEAD_IMG_BUCKET_URL + "/" + Constants.QINIU_HEAD_IMG_BUCKET_NAME + "/" + imgUrlName;
            String file_url = "http://" + Constants.QINIU_HEAD_IMG_BUCKET_URL + "/" + imgUrlName;
            return file_url;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new MaMaBikeException("头像上传失败");
        }
    }

}

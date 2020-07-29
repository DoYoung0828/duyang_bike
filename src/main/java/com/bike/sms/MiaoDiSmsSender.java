package com.bike.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bike.common.constants.Constants;
import com.bike.common.utils.HttpUtil;
import com.bike.security.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 实现发送短信接口(面向接口编程)
 */
@Service("verCodeService")
@Slf4j
public class MiaoDiSmsSender implements SmsSender {

    private static String operation = "/industrySMS/sendSMS";//短信类型(这里发送短信验证码)

    /**
     * 秒滴发送短信(重写短信发送接口)
     */
    @Override
    public void sendSms(String phone, String tplId, String params) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            String sig = MD5Util.getMD5(Constants.MDSMS_ACCOUNT_SID + Constants.MDSMS_AUTH_TOKEN + timestamp);
            String url = Constants.MDSMS_REST_URL + operation;
            Map<String, String> param = new HashMap<>();
            param.put("accountSid", Constants.MDSMS_ACCOUNT_SID);
            param.put("to", phone);
            param.put("templateid", tplId);//模板id
            param.put("param", params);
            param.put("timestamp", timestamp);
            param.put("sig", sig);
            param.put("respDataType", "json");//返回数据类型
            String result = HttpUtil.post(url, param);
            JSONObject jsonObject = JSON.parseObject(result);
            if (!jsonObject.getString("respCode").equals("00000")) {
                log.error("fail to send sms to " + phone + ":" + params + ":" + result);
            }
        } catch (Exception e) {
            log.error("fail to send sms to " + phone + ":" + params);
        }
    }

}

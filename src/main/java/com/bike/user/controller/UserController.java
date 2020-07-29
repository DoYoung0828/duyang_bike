package com.bike.user.controller;

import com.bike.common.constants.Constants;
import com.bike.common.exception.MaMaBikeException;
import com.bike.common.resp.ApiResult;
import com.bike.common.rest.BaseController;
import com.bike.user.entity.LoginInfo;
import com.bike.user.entity.User;
import com.bike.user.entity.UserElement;
import com.bike.user.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/user/")
public class UserController extends BaseController {

    @Autowired
    //@Qualifier("userServiceImpl")
    private UserService userService;

    /**
     * 1、用户登录(用户不存在时注册):
     * (1)登录存储token用户信息,返回token至前端,访问接口时带上token访问;
     */
    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @ApiImplicitParam(name = "loginInfo", value = "加密数据", required = true, dataType = "LoginInfo")
    @RequestMapping(value = "login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)//json格式
    public ApiResult login(@RequestBody LoginInfo loginInfo) {
        ApiResult<String> resp = new ApiResult<>();
        try {
            String data = loginInfo.getData();
            String key = loginInfo.getKey();
            if (data == null) {
                throw new MaMaBikeException("非法请求");//或者StringUtils.isBlank()校验
            }
            String token = userService.login(data, key);
            resp.setData(token);
        } catch (MaMaBikeException e) {
            resp.setCode(e.getStatusCode());//自定义异常类
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to login", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);//常量包
            resp.setMessage("内部错误");
        }
        return resp;
    }

    /**
     * 2、修改用户昵称:
     * (1)根据请求头中的token确定修改昵称用户;
     */
    @ApiOperation(value = "修改昵称", notes = "用户修改昵称", httpMethod = "POST")
    @ApiImplicitParam(name = "user", value = "用户信息 包含昵称", required = true, dataType = "User")
    @RequestMapping(value = "modifyNickName", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult modifyNickName(@RequestBody User user) {
        ApiResult<String> resp = new ApiResult<>();
        try {
            UserElement ue = getCurrentUser();//根据token获取user信息
            user.setId(ue.getUserId());//修改昵称id
            userService.modifyNickName(user);
            resp.setMessage("更新成功");
        } catch (MaMaBikeException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to update user info", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }

        return resp;
    }

    /**
     * 3、发送验证码
     */
    @ApiOperation(value = "短信验证码", notes = "根据用户手机号码发送验证码", httpMethod = "POST")
    @ApiImplicitParam(name = "user", value = "用户信息 包含手机号码", required = true, dataType = "User")
    @RequestMapping(value = "sendVercode", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult sendVercode(@RequestBody User user, HttpServletRequest request) {
        ApiResult<String> resp = new ApiResult<>();
        try {
            if (StringUtils.isEmpty(user.getMobile())) {
                throw new MaMaBikeException("手机号码不能为空");
            }
            userService.sendVercode(user.getMobile(), getIpFromRequest(request));
            resp.setMessage("发送成功");
        } catch (MaMaBikeException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to update user info", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

    /**
     * 4、修改用户头
     */
    @ApiOperation(value = "上传头像", notes = "用户上传头像 file", httpMethod = "POST")
    @RequestMapping("uploadHeadImg")
    public ApiResult<String> uploadHeadImg(HttpServletRequest request, @RequestParam(required = false) MultipartFile file) {
        ApiResult<String> resp = new ApiResult<>();
        try {
            //根据请求头header中的user-token获取用户信息
            UserElement ue = getCurrentUser();
            String file_url = userService.uploadHeadImg(file, ue.getUserId());
            resp.setFile_url(file_url);
            resp.setMessage("上传成功");
        } catch (MaMaBikeException e) {
            resp.setCode(e.getStatusCode());
            resp.setMessage(e.getMessage());
        } catch (Exception e) {
            log.error("Fail to update user info", e);
            resp.setCode(Constants.RESP_STATUS_INTERNAL_ERROR);
            resp.setMessage("内部错误");
        }
        return resp;
    }

}

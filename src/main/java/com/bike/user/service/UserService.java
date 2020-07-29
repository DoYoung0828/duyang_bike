package com.bike.user.service;

import com.bike.common.exception.MaMaBikeException;
import com.bike.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    /**
     * 1、用户登录(用户不存在时注册)
     */
    String login(String data, String key) throws MaMaBikeException;

    /**
     * 2、修改用户昵称
     */
    void modifyNickName(User user) throws MaMaBikeException;

    /**
     * 3、验证码发送
     */
    void sendVercode(String mobile, String ip) throws MaMaBikeException;

    /**
     * 4、修改用户头像
     */
    String uploadHeadImg(MultipartFile file, long userId) throws MaMaBikeException;

}

package com.bike.user.entity;

import lombok.Data;

/**
 * 登录参数
 */
@Data
public class LoginInfo {

    private String data;//aes加密的data

    private String key;//rsa加密的aes的密钥key

}

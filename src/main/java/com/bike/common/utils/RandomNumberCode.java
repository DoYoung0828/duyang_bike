package com.bike.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * 随机生成验证码工具类
 */
public class RandomNumberCode {

    //1、随机生成验证码(4位数)
    public static String verCode() {
        Random random = new Random();
        String randomCode = StringUtils.substring(String.valueOf(random.nextInt() * -10), 2, 6);
        return randomCode;
    }

    //2、单车业务生成随机值
    public static String randomNo() {
        Random random = new Random();
        return String.valueOf(Math.abs(random.nextInt() * -10));//随机出来有负数,abs取绝对值
    }

}

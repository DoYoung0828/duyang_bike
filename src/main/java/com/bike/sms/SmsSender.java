package com.bike.sms;

/**
 * 发送短信接口,面向接口编程
 */
public interface SmsSender {

    //手机号,短信模板id,参数
    void sendSms(String phone, String tplId, String params);

}

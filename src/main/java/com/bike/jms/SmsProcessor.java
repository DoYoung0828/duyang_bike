package com.bike.jms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bike.sms.SmsSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.bike.security.MD5Util.MD51;

/**
 * 短信发送者,队列监听者(既是生产者又是消费者)
 */
@Component(value = "smsProcessor")
public class SmsProcessor {

    @Autowired
    private JmsMessagingTemplate jmsTemplate;

    @Autowired
    @Qualifier("verCodeService")//注入发送短信接口的实现类
    private SmsSender smsSender;

    //1、短信发送
    public void sendSmsToQueue(Destination destination, final String message) {
        jmsTemplate.convertAndSend(destination, message);
    }

    //2、队列监听
    @JmsListener(destination = "sms.queue")
    public void doSendSmsMessage(String text) {
        JSONObject jsonObject = JSON.parseObject(text);
        smsSender.sendSms(jsonObject.getString("mobile"), jsonObject.getString("tplId"), jsonObject.getString("vercode"));
    }

    //3、公司短信发送接口
    public void sendMessage(String verCode,String mobile) {
        //接口地址
        String url = "http://60.205.14.180:9000/HttpSmsMt";
        //下发时间
        String mttime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Map<String, String> param = new HashMap<String, String>();
        param.put("name", "scstkj");
        param.put("pwd", MD51("3ed07a7ff40157d633b768581b1435e5" + mttime));//md51加密成新字符串
        try {
            param.put("content", URLEncoder.encode("【码码在线/du.yang】验证码:" + verCode + ",三分钟内有效，如非本人操作,请忽略此短信！", "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        param.put("phone", mobile);
        param.put("subid", "010");
        param.put("mttime", mttime);
        param.put("rpttype", "1");
        sendPost(url, param);//发送验证码请求
    }

    //消息发送接口,static才能被外包调用
    public static String sendPost(String url, Map<String, String> params) {
        URL u = null;
        HttpURLConnection con = null;
        // 构建请求参数
        StringBuffer sb = new StringBuffer();
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            }
            sb.substring(0, sb.length() - 1);
        }
        //尝试发送请求
        try {
            u = new URL(url);
            con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(6000);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            osw.write(sb.toString());
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        //读取返回内容
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                buffer.append(temp).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

}

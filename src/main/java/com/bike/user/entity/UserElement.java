package com.bike.user.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于缓存的user信息体
 */
@Data
public class UserElement {

    private long userId;

    private String mobile;

    private String token;

    private String platform;

    private String pushUserId;

    private String pushChannelId;

    /**
     * user对象转map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("platform", this.platform);
        map.put("userId", this.userId + "");
        map.put("token", token);
        map.put("mobile", mobile);
        if (this.pushUserId != null) {
            map.put("pushUserId", this.pushUserId);
        }
        if (this.pushChannelId != null) {
            map.put("pushChannelId", this.pushChannelId);
        }
        return map;
    }

    /**
     * map转user对象
     */
    public static UserElement fromMap(Map<String, String> map) {
        UserElement ue = new UserElement();
        ue.setPlatform(map.get("platform"));
        ue.setToken(map.get("token"));
        ue.setMobile(map.get("mobile"));
        ue.setUserId(Long.parseLong(map.get("userId")));
        ue.setPushUserId(map.get("pushUserId"));
        ue.setPushChannelId(map.get("pushChannelId"));
        return ue;
    }

}

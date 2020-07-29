package com.bike.common.constants;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统参数
 */
@Component//注入让spring能扫描到
@Data
public class Parameters {

    //1、redis配置属性
    @Value("${redis.host}")
    private String redisHost;
    @Value("${redis.port}")
    private int redisPort;
    @Value("${redis.auth}")
    private String redisAuth;
    @Value("${redis.max-idle}")
    private int redisMaxTotal;
    @Value("${redis.max-total}")
    private int redisMaxIdle;
    @Value("${redis.max-wait-millis}")
    private int redisMaxWaitMillis;

    //2、不需权限校验的路径
    @Value("#{'${security.noneSecurityPath}'.split(',')}")
    private List<String> noneSecurityPath;

}

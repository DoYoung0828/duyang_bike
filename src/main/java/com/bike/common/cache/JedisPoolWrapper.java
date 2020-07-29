package com.bike.common.cache;

import com.bike.common.constants.Parameters;
import com.bike.common.exception.MaMaBikeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;

@Component//注入让spring能扫描到
@Slf4j//记录日志
public class JedisPoolWrapper {

    private JedisPool jedisPool = null;

    @Autowired
    private Parameters parameters;

    //初始化连接池(启动即调用)
    @PostConstruct//保证在bean注入时,先初始化连接池(同静态代码块),就不用注入时调用初始化方法
    public void init() throws MaMaBikeException {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            //参数设置
            config.setMaxTotal(parameters.getRedisMaxTotal());
            config.setMaxIdle(parameters.getRedisMaxIdle());
            config.setMaxWaitMillis(parameters.getRedisMaxWaitMillis());

            jedisPool = new JedisPool(config,
                    parameters.getRedisHost(), parameters.getRedisPort(), 2000, parameters.getRedisAuth());
        } catch (Exception e) {
            log.error("Fail to initialize jedis pool", e);
            throw new MaMaBikeException("Fail to initialize jedis pool");
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

}

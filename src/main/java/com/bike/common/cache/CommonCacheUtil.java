package com.bike.common.cache;

import com.bike.common.exception.MaMaBikeException;
import com.bike.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Map;

@Component//注入让spring能扫描到
@Slf4j//记录日志
public class CommonCacheUtil {

    private static final String TOKEN_PREFIX = "token.";
    private static final String USER_PREFIX = "user.";

    @Autowired
    private JedisPoolWrapper jedisPoolWrapper;

    /**
     * 1、获取缓存value:手机号码为key,验证码为value
     */
    public String getCacheValue(String key) {
        String value = null;
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis Jedis = pool.getResource()) {
                    Jedis.select(0);
                    value = Jedis.get(key);
                }
            }
        } catch (Exception e) {
            log.error("Fail to get cached value", e);
        }
        return value;
    }

    /**
     * 2、登录时存储token及用户信息
     */
    public void putTokenWhenLogin(UserElement ue) {
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);//缓存信息存储片区(不写默认索引为0片区)
                Transaction trans = jedis.multi();//redis事务
                try {
                    trans.del(TOKEN_PREFIX + ue.getToken());
                    trans.hmset(TOKEN_PREFIX + ue.getToken(), ue.toMap());//user转map存redis里面
                    trans.expire(TOKEN_PREFIX + ue.getToken(), 2592000);//3天
                    trans.sadd(USER_PREFIX + ue.getUserId(), ue.getToken());
                    trans.exec();
                } catch (Exception e) {
                    trans.discard();
                    log.error("Fail to cache token to redis", e);
                }
            }
        }
    }

    /**
     * 3、根据token取redis缓存的用户信息
     */
    public UserElement getUserByToken(String token) throws MaMaBikeException {
        UserElement ue = null;
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                try {
                    //根据token获取user信息
                    Map<String, String> map = jedis.hgetAll(TOKEN_PREFIX + token);
                    if (map != null && !map.isEmpty()) {
                        ue = UserElement.fromMap(map);//map->user
                    } else {
                        log.warn("Fail to find cached element for token {}", token);
                    }
                } catch (Exception e) {
                    log.error("Fail to get token from redis", e);
                    throw new MaMaBikeException("Fail to get token content");
                }
            }
        }
        return ue;
    }

    /**
     * 4、缓存手机验证码专用,限制了发送次数:
     * (1)返回值:1-当前验证码未过期
     * ---------2-手机号超过当日验证码次数上限
     * ---------3-ip超过当日验证码次数上线
     * (2)参数:type-统计同一手机短信发送次数(key:verify.code.17729842954.reg)
     * -------timeout-设定短信过期时间(redis中存储时间)
     * -------ip-统计同一ip发送短信次数(key:ip.127.0.0.1)
     */
    public int cacheForVerificationCode(String key, String value, String type, int timeout, String ip) throws MaMaBikeException {
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.select(0);
                    String ipKey = "ip." + ip;
                    if (ip == null) {
                        return 3;
                    } else {
                        String ipSendCount = jedis.get(ipKey);
                        try {
                            //同一ip发送短信不超过10次
                            if (ipSendCount != null && Integer.parseInt(ipSendCount) >= 25) {
                                return 3;
                            }
                        } catch (NumberFormatException e) {
                            log.error("Fail to process ip send count", e);
                            return 3;
                        }
                    }

                    //key-手机号,保存验证码
                    long succ = jedis.setnx(key, value);
                    if (succ == 0) {
                        return 1;
                    }

                    String sendCount = jedis.get(key + "." + type);
                    try {
                        if (sendCount != null && Integer.parseInt(sendCount) >= 25) {
                            jedis.del(key);
                            return 2;
                        }
                    } catch (NumberFormatException e) {
                        log.error("Fail to process send count", e);
                        jedis.del(key);
                        return 2;
                    }

                    try {
                        //trans.set(key, value);
                        jedis.expire(key, timeout);//redis验证码保存时间
                        long val = jedis.incr(key + "." + type);
                        if (val == 1) {
                            jedis.expire(key + "." + type, 86400);
                        }

                        jedis.incr(ipKey);
                        if (val == 1) {
                            jedis.expire(ipKey, 86400);
                        }
                    } catch (Exception e) {
                        log.error("Fail to cache data into redis", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Fail to cache for expiry", e);
            throw new MaMaBikeException("Fail to cache for expiry");
        }

        return 0;
    }


    /**
     * 缓存 key-value 永久
     */
    public void cache(String key, String value) {
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis Jedis = pool.getResource()) {
                    Jedis.select(0);//默认第0片区(redis有15个片区)
                    Jedis.set(key, value);
                }
            }
        } catch (Exception e) {
            log.error("Fail to cache value", e);
        }
    }

    /**
     * 设置key value 以及过期时间
     *
     * @param key
     * @param value
     * @param expiry
     * @return
     */
    public long cacheNxExpire(String key, String value, int expiry) {
        long result = 0;
        try {
            JedisPool pool = jedisPoolWrapper.getJedisPool();
            if (pool != null) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.select(0);
                    result = jedis.setnx(key, value);
                    jedis.expire(key, expiry);
                }
            }
        } catch (Exception e) {
            log.error("Fail to cacheNx value", e);
        }

        return result;
    }

    /**
     * 删除缓存key
     *
     * @param key
     */
    public void delKey(String key) {
        JedisPool pool = jedisPoolWrapper.getJedisPool();
        if (pool != null) {

            try (Jedis jedis = pool.getResource()) {
                jedis.select(0);
                try {
                    jedis.del(key);
                } catch (Exception e) {
                    log.error("Fail to remove key from redis", e);
                }
            }
        }
    }


}

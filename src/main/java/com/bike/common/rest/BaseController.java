package com.bike.common.rest;

import com.bike.common.cache.CommonCacheUtil;
import com.bike.common.constants.Constants;
import com.bike.common.exception.MaMaBikeException;
import com.bike.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 操作redis缓存
 */
@Slf4j//记录日志
public class BaseController {

    @Autowired//操作缓存,引入CommonCacheUtil类
    private CommonCacheUtil cacheUtil;

    /**
     * 1、根据token获取用户信息:
     * (1)BaseController只有继承才能使用,默认就是protected;
     * (2)访问请求带着登录成功返回的token,一般存于请求头header中;
     */
    protected UserElement getCurrentUser() throws MaMaBikeException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.REQUEST_TOKEN_KEY);
        if (StringUtils.isNotEmpty(token)) {
            try {
                UserElement ue = cacheUtil.getUserByToken(token);
                return ue;
            } catch (Exception e) {
                log.error("fail to get user by token", e);
                throw e;
                //return null;
            }
        }
        return null;
    }

    /**
     * 2、获取请求客户端的ip地址
     */
    protected String getIpFromRequest(HttpServletRequest request) {
        //避免反向代理,将ip屏蔽情况(nginx转发请求,暴露虚拟ip)
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        //当ip="0:0:0:0:0:0:0:1",为本地ip地址情况
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }

}

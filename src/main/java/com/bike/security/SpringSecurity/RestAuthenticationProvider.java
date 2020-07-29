package com.bike.security.SpringSecurity;

import com.bike.common.exception.BadCredentialException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * 二、校验token是否合法,再授权处理
 */
public class RestAuthenticationProvider implements AuthenticationProvider {

    /**
     * 1、校验授权token是不是合法,符合要求
     */
    @Override
    public boolean supports(Class<?> authentication) {
        System.out.println(66666);//当不支持时,返回false,不授权
        //授权不要忘了自定义token
        Boolean bo = PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication) ||
                RestAuthenticationToken.class.isAssignableFrom(authentication);
        return bo;
    }

    /**
     * 2、对符合要求的token用户授权
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println(77777);
        //判定传递的authentication类型是否是预授权类型
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken preAuth = (PreAuthenticatedAuthenticationToken) authentication;
            //获取到token
            RestAuthenticationToken sysAuth = (RestAuthenticationToken) preAuth.getPrincipal();
            if (sysAuth.getAuthorities() != null && sysAuth.getAuthorities().size() > 0) {
                GrantedAuthority gauth = sysAuth.getAuthorities().iterator().next();
                if ("BIKE_CLIENT".equals(gauth.getAuthority())) {
                    return sysAuth;
                } else if ("ROLE_SOMEONE".equals(gauth.getAuthority())) {
                    return sysAuth;
                }
            }
        } else if (authentication instanceof RestAuthenticationToken) {
            RestAuthenticationToken sysAuth = (RestAuthenticationToken) authentication;
            if (sysAuth.getAuthorities() != null && sysAuth.getAuthorities().size() > 0) {
                GrantedAuthority gauth = sysAuth.getAuthorities().iterator().next();
                if ("BIKE_CLIENT".equals(gauth.getAuthority())) {
                    return sysAuth;
                } else if ("ROLE_SOMEONE".equals(gauth.getAuthority())) {
                    return sysAuth;
                }
            }
        }
        throw new BadCredentialException("unknown.error");
    }

}

package com.bike.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * SpringSecurity的provider授权中,token校验自定义授权异常
 */
public class BadCredentialException extends AuthenticationException {

    public BadCredentialException(String msg) {
        super(msg);
    }

}

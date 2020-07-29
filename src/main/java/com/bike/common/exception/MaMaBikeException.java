package com.bike.common.exception;

import com.bike.common.constants.Constants;

/**
 * 自定义异常
 */
public class MaMaBikeException extends Exception {

    private static final long serialVersionUID = -7370331410579650067L;

    //构造函数
    public MaMaBikeException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return Constants.RESP_STATUS_INTERNAL_ERROR;//500
    }

}

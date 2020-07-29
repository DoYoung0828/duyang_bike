package com.bike.common.resp;

import com.bike.common.constants.Constants;
import lombok.Data;

@Data
public class ApiResult<T> {

    private int code = Constants.RESP_STATUS_OK;

    private String message;

    private T data;

    //七牛云返回路径
    private String file_url;

}

package com.bike.common.swagger;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import springfox.documentation.spring.web.json.Json;

/**
 * Created by JackWangon[www.coder520.com] 2017/8/28.
 */
public class FastJsonHttpMessageConverterEx extends FastJsonHttpMessageConverter {

    public FastJsonHttpMessageConverterEx() {
        super();
        this.getFastJsonConfig().getSerializeConfig().put(Json.class, SwaggerJsonSerializer.instance);
    }

}

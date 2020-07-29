package com.bike;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.bike.common.swagger.FastJsonHttpMessageConverterEx;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
//@MapperScan(value = "com.bike.user.dao")//扫描数据层dao文件夹下或者用@Mapper注解让其扫描该层
@EnableTransactionManagement
@PropertySource(value = "classpath:parameter.properties")//加载无需校验的路径集
public class BikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BikeApplication.class, args);
    }

    /**
     * 1、用于properties文件占位符解析(使得支持参数配置类中'#',split(',')等特殊字符)
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * FastJsonHttpMessageConverter
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverterEx();
        HttpMessageConverter<?> converter = fastConverter;
        return new HttpMessageConverters(converter);
    }

}

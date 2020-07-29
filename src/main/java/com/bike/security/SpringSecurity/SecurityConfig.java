package com.bike.security.SpringSecurity;

import com.bike.common.cache.CommonCacheUtil;
import com.bike.common.constants.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * 一、预授权(在其他系统或者代码中已进行授权)
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Parameters parameters;

    @Autowired
    private CommonCacheUtil commonCacheUtil;

    /**
     * 1、设置Manager里面的provider(添加provider),启动时运行
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println(11111);
        auth.authenticationProvider(new RestAuthenticationProvider());
    }

    /**
     * 2、安全配置,启动时运行
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println(22222);
        //定义一个数组,数组容量大小是无需校验url集的size()
        String[] num = new String[parameters.getNoneSecurityPath().size()];
        http.csrf().disable()//防止脚本攻击,表单唯一性,防止表单重复提交
                .authorizeRequests()//校验所有请求
                .antMatchers(parameters.getNoneSecurityPath().toArray(num)).permitAll()//无需校验请求全部放行(login等)
                .anyRequest().authenticated()//除免校验url,其他路径全部校验
                //无状态session,不需要创建,这里仅校验token(STATELESS)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().httpBasic().authenticationEntryPoint(new RestAuthenticationEntryPoint())//添加异常统一处理
                .and().addFilter(getPreAuthenticatedProcessingFilter());//添加自定义登录验证过滤器
    }

    /**
     * 3、为验证过滤器设置AuthenticationManager(由于用了springboot注入方式),启动时运行
     */
    private RestPreAuthenticatedProcessingFilter getPreAuthenticatedProcessingFilter() throws Exception {
        System.out.println(33333);
        RestPreAuthenticatedProcessingFilter filter = new RestPreAuthenticatedProcessingFilter(parameters.getNoneSecurityPath(), commonCacheUtil);
        filter.setAuthenticationManager(this.authenticationManagerBean());
        return filter;
    }

    /**
     * 4、忽略,OPTIONS方法的请求,启动时运行
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        System.out.println(44444);
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
        //.antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/configuration/**", "/swagger-ui.html", "/webjars/**");
        //放过swagger
    }

}

package com.bike.security.SpringSecurity;

import com.bike.common.cache.CommonCacheUtil;
import com.bike.common.constants.Constants;
import com.bike.common.constants.Parameters;
import com.bike.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 二、用户自定义登陆验证过滤器:
 * 注意:用户登陆,会被AuthenticationProcessingFilter拦截,
 * 调用AuthenticationManager的实现,而AuthenticationManager会调用ProviderManager来获取用户验证信息,
 * 不同的Provider调用的服务不同,这些信息可以是在数据库上,可以是在LDAP服务器上,可以是xml配置文件上等,
 * 如果验证通过后会将用户的权限信息封装一个User放到spring的全局缓存SecurityContextHolder中,以备后面访问资源时使用
 */
@Slf4j
public class RestPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    //不能这样直接注入,这里优先级很高(或在spring容器之外),spring容器还没有启动,故注入不会有效
    //解决方式是外面注入,由构造器引入
    //@Autowired
    //private Parameters parameters;

    //spring的路径匹配器
    private AntPathMatcher matcher = new AntPathMatcher();

    //引入参数配置
    private List<String> noneSecurityList;
    private CommonCacheUtil commonCacheUtil;

    //构造器引入
    public RestPreAuthenticatedProcessingFilter(List<String> noneSecurityList, CommonCacheUtil commonCacheUtil) {
        this.noneSecurityList = noneSecurityList;
        this.commonCacheUtil = commonCacheUtil;
    }

    /**
     * 1、获取用户传过来的信息,生成token
     */
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        System.out.println(55555);
        GrantedAuthority[] authorities = new GrantedAuthority[1];//角色个数(手机端只有用户1个角色,不存在权限)
        if (isNoneSecurity(request.getRequestURI().toString()) || "OPTIONS".equals(request.getMethod())) {
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SOMEONE");//任意角色
            authorities[0] = authority;//角色赋予权限(这里就一个角色用户故authorities[0])
            //无需权限的url直接发放token走Provider授权
            return new RestAuthenticationToken(Arrays.asList(authorities));//这里参数是集合,声明数组->集合也一样
        }

        //检查APP版本(版本控制,较低提醒升级)
        String version = request.getHeader(Constants.REQUEST_VERSION_KEY);
        if (version == null) {
            //版本有误
            request.setAttribute("header-error", 400);
        }

        //校验token
        String token = request.getHeader(Constants.REQUEST_TOKEN_KEY);
        if (request.getAttribute("header-error") == null) {
            try {
                if (token != null && !token.trim().isEmpty()) {
                    //token存在时,根据token获取用户user对象(redis缓存中获取)
                    UserElement ue = commonCacheUtil.getUserByToken(token);
                    //判断缓存中获取的user对象类型是否是UserElement类型(redis校验token即可,这里再次校验对象类型有多余成分)
                    if (ue instanceof UserElement) {
                        //检查到token说明用户已经登录,授权给用户BIKE_CLIENT角色(单车用户),允许访问
                        GrantedAuthority authority = new SimpleGrantedAuthority("BIKE_CLIENT");
                        authorities[0] = authority;
                        RestAuthenticationToken authToken = new RestAuthenticationToken(Arrays.asList(authorities));
                        authToken.setUser(ue);//用户保存至token
                        return authToken;
                    } else {
                        //token不对
                        request.setAttribute("header-error", 401);
                    }
                } else {
                    //token不存在时,告诉移动端,跳转登录页面,重新登录
                    log.warn("Got no token from request header");
                    request.setAttribute("header-error", 401);
                }
            } catch (Exception e) {
                log.error("Fail to authenticate user", e);
            }

        }

        //当请求有误时
        if (request.getAttribute("header-error") != null) {
            //请求头有错误,随便给个角色,让逻辑继续
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_NONE");//什么角色都不是
            authorities[0] = authority;
        }
        RestAuthenticationToken authToken = new RestAuthenticationToken(Arrays.asList(authorities));
        return authToken;
    }

    /**
     * 2、获取用户授权凭证(基本不用)
     */
    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return null;
    }

    /**
     * 3、校验是否是无需校验的url
     */
    private boolean isNoneSecurity(String uri) {
        boolean result = false;
        if (this.noneSecurityList != null) {
            for (String pattern : this.noneSecurityList) {
                if (matcher.match(pattern, uri)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}

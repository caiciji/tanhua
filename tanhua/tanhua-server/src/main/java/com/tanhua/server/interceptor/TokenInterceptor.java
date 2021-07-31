package com.tanhua.server.interceptor;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.db.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 在进入controller方法前执行
     * 从请求头中获取token，根据token从redis中获取用户信息,如果得到用户信息则存入线程,放行
     *
     * 如果得不到登录用户信息则返回401，阻止请求
     * @param request
     * @param response
     * @param handler
     * @return true 可以访问controller，fasle不能访问controller方法
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("请求的路径:"+request.getRequestURI());
        String token = request.getHeader("Authorization");
        log.debug(token);
        if(StringUtils.isNotEmpty(token)){
            //token有值才需要处理
            User user = getUserByToken(token);
            log.debug("登录用户:{}",user);
            //token没有失效
            if(null !=user){
                UserHolder.setUser(user);
                return true;//放行
            }
        }
        //401没有认证，要用户重新登录
        response.setStatus(401);
        return false;
    }

    /**
     * 通过token获取登录用户信息
     * @param token
     * @return
     */
    public User getUserByToken(String token){

        String tokenKey = "TOKEN_" + token;

        String userJsonString = (String) redisTemplate.opsForValue().get(tokenKey);

        if(null ==userJsonString){

            throw new TanHuaException("登录超时，请重新登录");
        }

        redisTemplate.expire(tokenKey,7, TimeUnit.DAYS);


        User loginUser = JSON.parseObject(userJsonString, User.class);
        return loginUser;
    }
}

package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX="MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ","");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);
        Admin admin = null;
        if(StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey,30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 用户登录
     * @param paramMap
     * @return
     */
    public Map<String,String> login(Map<String, String> paramMap) {
        //1.收集前端传过来的数据
        String username = paramMap.get("username");
        String password = paramMap.get("password");
        String verificationCode = paramMap.get("verificationCode");
        String uuid = paramMap.get("uuid");
        //2.校验验证码
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        //是否失效
        if(StringUtils.isEmpty(codeInRedis)){
            throw new BusinessException("验证码已失效");
        }
        if(!StringUtils.equals(codeInRedis,verificationCode)){
            throw new BusinessException("验证码输入有误，请重新输入");
        }
        //删除redis中的验证码
        redisTemplate.delete(key);
        //3.检验用户名或密码是否正确
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            throw new BusinessException("用户名或密码不能为空");
        }
        //调用mapper用户查询
        Admin adminDB = query().eq("username", username).one();

        //先解密md5的密码
        String encryptedPassword = SecureUtil.md5(password);
        if(null == adminDB || !StringUtils.equals(encryptedPassword,adminDB.getPassword())){
            throw new BusinessException("用户名或密码不正确");
        }

        //4.校验通过，签发token，设置有效期
        String token = jwtUtils.createJWT(adminDB.getUsername(), adminDB.getId());
        //4.1【注意】存入redis必须与取出的token一致
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminJsonString = JSON.toJSONString(adminDB);
        //设置redis有效期30分钟
        redisTemplate.opsForValue().set(tokenKey,adminJsonString,30,TimeUnit.MINUTES);
        Map<String,String> tokenMap=new HashMap<>();
        tokenMap.put("token",token);
        return tokenMap;
    }

    /**
     * 用户退出登录
     * @param token
     */
    public void logout(String token) {
        log.info("token"+token);
        token = token.replace("Bearer ", "");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        log.info("tokenKey"+tokenKey);
        Boolean delete = redisTemplate.delete(tokenKey);
        log.error("delete"+delete);
    }
}

package com.tanhua.server.service;

import com.aliyuncs.utils.StringUtils;
import com.tanhua.commons.constant.RedisKeyConst;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.*;
import com.tanhua.domain.vo.ErrorResult;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVoAge;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.interceptor.UserHolder;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SettingsService {

   @Reference
   private SettingApi settingApi;

   @Reference
   private QuestionApi questionApi;

   @Reference
   private BlackListApi blackListApi;

   @Reference
   private UserInfoApi userInfoApi;

   @Reference
   private UserApi userApi;

   @Autowired
   private RedisTemplate<String,String> redisTemplate;

   @Autowired
   private SmsTemplate smsTemplate;

    /**
     * 读取通用设置
     * @return
     */
    public SettingsVo querySettings() {
        //1.获取登录用户
        User loginUser = UserHolder.getUser();
        //2.查询登陆用户的陌生人问题
        String strangerQuestion="你喜欢我吗?";//默认的陌生人的问题
      Question question= questionApi.findByUserId(loginUser.getId());
      if(null !=question){
          //用户设置，则使用用户设置的
          strangerQuestion = question.getTxt();
      }
      //3.查询登录用户的通知设置
      Settings settings= settingApi.findByUserId(loginUser.getId());
      //4.构建vo
        SettingsVo vo=new SettingsVo();
        vo.setStrangerQuestion(strangerQuestion);
        //有痛着设置时才需要来设置，否则采用默认值true
        if(null !=settings){
            BeanUtils.copyProperties(settings,vo);
        }
        //5.设置用户id，手机号码
        vo.setId(loginUser.getId());
        vo.setPhone(loginUser.getMobile());
        //6.返回vo
        return vo;
    }

    /**
     * 保存通用设置
     * @param vo
     */
    public void updateNotification(SettingsVo vo) {
        //1.把vo转成pojo
        Settings settings=new Settings();
        BeanUtils.copyProperties(vo,settings);
        //2.设置操作的用户id为登录id
        settings.setUserId(UserHolder.getUserId());
        //3.调用api
        settingApi.save(settings);
    }

    /**
     * 保存陌生人问题
     * @param paramMap
     */
    public void updateQuersion(Map<String, String> paramMap) {
        //1.构建pojo
        Question question=new Question();
        question.setTxt(paramMap.get("content"));
        question.setUserId(UserHolder.getUserId());

        //2.调用api保存
        questionApi.save(question);
    }

    /**
     * 黑名单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<UserInfoVoAge> blackList(Long page, Long pageSize) {
        //1.调用api通过登录用户id，分页查询登录用户的黑名单列表
       PageResult pageResult= blackListApi.findPageByUserId(UserHolder.getUserId(),page,pageSize);

       //2.获取分页的结果集，里有黑名单人员的用户id
        List<BlackList> blcakLists = pageResult.getItems();

        //3.补全黑名单人员的用户详情
        //isEmpty为空
        if(!Collections.isEmpty(blcakLists)){
            //有黑名单
            //批量查询黑名单人员的详情
            List<UserInfoVoAge> voList=new ArrayList<>(blcakLists.size());
            for (BlackList blackList : blcakLists) {
                //遍历黑名单人员，取出黑名单人员的id
                Long blackUserId = blackList.getBlackUserId();
                //查询这个黑名单人员的信息
                UserInfo blackUserInfo = userInfoApi.findById(blackUserId);
                //转成vo
                UserInfoVoAge vo=new UserInfoVoAge();
                BeanUtils.copyProperties(blackUserInfo,vo);
                voList.add(vo);
            }
            //重置设置查询的结果
            pageResult.setItems(voList);

        }
        return pageResult;
    }


    /**
     * 移除黑名单
     * @param blacklistById
     */
    public void deleteBlacklist(Long blacklistById) {
        //1.获取当前登录的id
        Long userId = UserHolder.getUserId();
        //2.调用api
        blackListApi.delete(userId,blacklistById);
    }





    /**
     *  修改手机号码:1.发送验证码
     */
    public void sendValidateCode() {
        String mobile = UserHolder.getUser().getMobile();
        //1.获取redis存入验证码的key
        String key = RedisKeyConst.CHANGE_MOBILE_VALIDATE_CODE + mobile;
        //redis中的验证码
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);

        log.debug("=====redis中的验证码 修改手机号码:{},{}",mobile,codeInRedis);

        if(StringUtils.isNotEmpty(codeInRedis)){
            //已经发送过了
            //return ErrorResult.duplicate();
            throw new TanHuaException(ErrorResult.duplicate());
        }
        //生成验证码
        String validateCode="123456"; /*RandomStringUtils.randomNumeric(6);*/

        //发送验证码
        Map<String ,String> smsResult=smsTemplate.sendValidateCode(mobile,validateCode);

        if(null !=smsResult){
            //发送失败
            throw new TanHuaException(ErrorResult.fail());
        }
        //存入redis，有效期5分钟
        redisTemplate.opsForValue().set(key,validateCode,5, TimeUnit.MINUTES);

    }


    /**
     * 修改手机号码:2.校验验证码
     * @param verificationCode
     * @return
     */
    public boolean checkValidateCode(String verificationCode) {
        User user = UserHolder.getUser();
        String phone = user.getMobile();
        //conredis中存入验证码的key
        String key = RedisKeyConst.CHANGE_MOBILE_VALIDATE_CODE + phone;
        //redis中的验证码
        String codeInRedis = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);//防止重复提交

        log.debug("==========修改手机号 校验 验证码:{},{},{}",phone,codeInRedis,verificationCode);
        if(StringUtils.isEmpty(codeInRedis)){
            throw new TanHuaException(ErrorResult.loginError());
        }

        if(!codeInRedis.equals(verificationCode)){
            return false;
        }
        return true;

    }

    /**
     * 修改手机号码：3.保存
     * @param phone
     * @param token
     */
    public void changeMobile(String phone, String token) {
        Long userId = UserHolder.getUserId();
        userApi.updateMobile(userId,phone);

        String key = RedisKeyConst.TOKEN + token;
        redisTemplate.delete(key);
    }


}

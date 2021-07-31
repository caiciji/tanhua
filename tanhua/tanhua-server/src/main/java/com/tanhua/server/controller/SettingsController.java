package com.tanhua.server.controller;

import com.tanhua.commons.constant.RedisKeyConst;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVoAge;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.SettingsService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class SettingsController {


    @Autowired
    private SettingsService settingsService;
    /**
     * 读取通用设置
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity querySettings(){

      SettingsVo vo= settingsService.querySettings();
      return ResponseEntity.ok(vo);
    }


    /**
     * 保存通用设置
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity updateNotification(@RequestBody SettingsVo vo){

        settingsService.updateNotification(vo);
        return ResponseEntity.ok(null);
    }

    /**
     * 保存陌生人问题
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity updateQuestion(@RequestBody Map<String,String> paramMap){
        settingsService.updateQuersion(paramMap);
        return ResponseEntity.ok(null);
    }


    /**
     * 黑名单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/blacklist")
    public ResponseEntity blacklist(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10") Long pageSize){
        page =page>0?page:1;//如果页码<=0 查询第一页
        pageSize=pageSize>0?pageSize:10;//负数处理
        pageSize=pageSize>50?50:pageSize;//pageSize限制大小，防止用户查询过多导致系统问题

        PageResult<UserInfoVoAge> pageResult=settingsService.blackList(page,pageSize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     *
     * 移除黑名单
     * @return
     */
    @DeleteMapping("/blacklist/{blacklistById}")
    public ResponseEntity deleteBlacklist(@PathVariable Long blacklistById){
        settingsService.deleteBlacklist(blacklistById);

        return ResponseEntity.ok(null);
    }


    /**
     * 修改手机号码:1.发送验证码
     * @return
     */
    @PostMapping("/phone/sendVerificationCode")
    public ResponseEntity sendValidateCode(){
        settingsService.sendValidateCode();
        return ResponseEntity.ok(null);
    }

    /***
     * 修改手机号码:2.校验验证码
     */
    @PostMapping("/phone/checkVerificationCode")
    public  ResponseEntity checkValidateCode(@RequestBody Map<String,String> param){

        boolean flag=settingsService.checkValidateCode(param.get("verificationCode"));
        Map<String,Boolean> result=new HashMap<String,Boolean>();
        result.put("verification",flag);
        return ResponseEntity.ok(result);
    }

    /**
     * 修改手机号码：3.保存
     */
    @PostMapping("/phone")
    public ResponseEntity changeMobile(@RequestBody Map<String,String> param,@RequestHeader("Authorization") String token){

        settingsService.changeMobile(param.get("phone"),token);
        return ResponseEntity.ok(null);


    }
}

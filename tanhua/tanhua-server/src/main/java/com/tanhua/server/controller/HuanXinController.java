package com.tanhua.server.controller;

import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.server.interceptor.UserHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/huanxin")
public class HuanXinController {


    /**
     * 获取当前登录的用户名与密码，用于环信的登录
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<HuanXinUser> getLoginHuanXinUser(){
        HuanXinUser user=new HuanXinUser(UserHolder.getUserId().toString(),"123456",String.format("赵得柱_%05d",UserHolder.getUserId()));
        return ResponseEntity.ok(user);
    }
}

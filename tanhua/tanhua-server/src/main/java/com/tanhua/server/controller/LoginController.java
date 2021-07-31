package com.tanhua.server.controller;

import com.tanhua.server.service.UserService;
import com.tanhua.domain.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制层
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 通过手机号码查询
     * @param phone
     * @return
     */
    @GetMapping("/findByMobile")
    public ResponseEntity findUser(String phone){
      User user=userService.findUser(phone);
      return ResponseEntity.ok(user);
    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity saveUser(@RequestBody User user){
         userService.saveUser(user);
         return ResponseEntity.ok(null);
    }

    /**
     * 注册登录和发送验证码
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity sendValidateCode(@RequestBody Map<String,String> paramMap){
        String phone = paramMap.get("phone");

        userService.sendValidateCode(phone);
        return ResponseEntity.ok(null);
    }


    /**
     * 登录和验证码校验
     * @return
     */
    @PostMapping("/loginVerification")
    public ResponseEntity loginVerification(@RequestBody Map<String,String> paramMap){

        String phone = paramMap.get("phone");

        String verificationCode = paramMap.get("verificationCode");

     Map<String,Object> result=  userService.loginVerification(phone,verificationCode);
     return ResponseEntity.ok(result);
    }
}

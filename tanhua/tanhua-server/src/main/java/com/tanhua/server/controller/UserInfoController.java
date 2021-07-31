package com.tanhua.server.controller;

import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
public class UserInfoController {


    @Autowired
    private UserService userService;

    /**
     * 查询用户信息-获取个人信息
     */
    @GetMapping
    public ResponseEntity getUserInfo(String userID,String huanxinID){

      UserInfoVo Vo= userService.getLoginUserInfo();
      return ResponseEntity.ok(Vo);
    }


    /**
     * 更新用户
     * @param userInfoVo
     * @return
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfoVo userInfoVo){

        userService.updateUserInfo(userInfoVo);

        return ResponseEntity.ok(null);
    }

    /**
     * 更新头像
     * @return
     */
    @PostMapping("/header")
    public ResponseEntity updateUserHeader(MultipartFile headPhoto){

        userService.updateUserHeader(headPhoto);

        return ResponseEntity.ok(null);
    }

}

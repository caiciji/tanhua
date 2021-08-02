package com.tanhua.server.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
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

    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     * @return
     */
    @GetMapping("/counts")
    public ResponseEntity counts(){
       CountsVo countsVo=userService.counts();
       return ResponseEntity.ok(countsVo);
    }

    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity queryUserLikeList(@PathVariable int type,@RequestParam(value ="page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10")Long pageSize){
      PageResult<FriendVo> vo=userService.queryUserLikeList(type,page,pageSize);
      return ResponseEntity.ok(vo);
    }

    /**
     * 粉丝 - 喜欢
     * @param fansId
     * @return
     */
    @PostMapping("/fans/{fansId}")
    public ResponseEntity fansLike(@PathVariable Long fansId){
        userService.fansLike(fansId);
        return ResponseEntity.ok(null);
    }
}

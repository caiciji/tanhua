package com.tanhua.server.controller;

import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class IMController {

    @Autowired
    private IMService imService;

    /**
     * 添加联系人
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity makeFriends(@RequestBody Map<String,Long> paramMap){
        imService.makeFriends(paramMap.get("userId"));
        return ResponseEntity.ok(null);
    }


    /**
     * 查看联系人
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    @GetMapping("/contacts")
    public ResponseEntity queryContactsList(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10")Long pageSize,String keyword){
       PageResult<ContactVo> pageResult= imService.queryContactsList(page,pageSize,keyword);
       return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询点赞列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity likes(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10")Long pageSize){
        PageResult<MessageVo> pageResult=imService.messageCommentList(1,page,pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询评论列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity comments(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10")Long pageSize){
        PageResult<MessageVo> pageResult=imService.messageCommentList(2,page,pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 查询喜欢列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity loves(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10")Long pageSize){
        PageResult<MessageVo> pageResult=imService.messageCommentList(3,page,pageSize);
        return ResponseEntity.ok(pageResult);
    }
}

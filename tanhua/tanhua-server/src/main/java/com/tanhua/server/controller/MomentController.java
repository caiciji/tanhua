package com.tanhua.server.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/movements")
public class MomentController {

    @Autowired
    private MomentService momentService;


    @Autowired
    private CommentService commentService;


    /**
     * 发布动态
     * @param publishVo
     * @param imageContent
     * @return
     */
    @PostMapping
    public ResponseEntity postMapping(PublishVo publishVo, MultipartFile[] imageContent){
     momentService.postMapping(publishVo,imageContent);

        return ResponseEntity.ok(null);
    }


    /**
     * 好友 动态
     */
    @GetMapping()
    public ResponseEntity queryFriendPublishList(@RequestParam(value="page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "1")Long pageSize){
        PageResult<MomentVo> pageResult=momentService.queryFriendPublishList(page,pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 推荐 动态
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/recommend")
    public ResponseEntity queryRecommendPublishList(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "1")Long pageSize){
        PageResult<MomentVo> pageResult=momentService.queryRecommendPublishList(page,pageSize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 查询用户动态
     * @param page
     * @param pageSize
     * @param userId
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity queryMyPublishList(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value ="pagesize",defaultValue = "1")Long pageSize,Long userId){
        PageResult<MomentVo> pageResult=momentService.queryMyPublishList(page,pageSize,userId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态 点赞
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/like")
    public  ResponseEntity like(@PathVariable String publishId){
       long likeCount= commentService.like(publishId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 动态 取消点赞
     */
    @GetMapping("/{publishId}/dislike")
    public ResponseEntity dislike(@PathVariable String publishId){
       long likeCount= commentService.dislike(publishId);
       return ResponseEntity.ok(likeCount);
    }

    /**
     * 动态 喜欢
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/love")
    public ResponseEntity love(@PathVariable String publishId){
      long loveCount=  commentService.love(publishId);
      return ResponseEntity.ok(loveCount);
    }


    /**
     * 动态 取消喜欢
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}/unlove")
    public ResponseEntity unlove(@PathVariable String publishId){
       long loveCount= commentService.unlove(publishId);
       return ResponseEntity.ok(loveCount);
    }

    /**
     * 通过 id 查询单条动态
     *
     * 这个api与最近访客(/visitors)功能冲突
     * @param publishId
     * @return
     */
    @GetMapping("/{publishId}")
    public ResponseEntity findById(@PathVariable String publishId){
      MomentVo vo=  momentService.findById(publishId);
      return ResponseEntity.ok(vo);
    }

    /**
     * 防止后台报错
     * 第9天才实现
     * 查询谁看过我
     * @return
     */
    @GetMapping("/visitors")
    public ResponseEntity visitors(){
        List<VisitorVo> vo =momentService.queryVisitors();
        return ResponseEntity.ok(vo);
    }
}

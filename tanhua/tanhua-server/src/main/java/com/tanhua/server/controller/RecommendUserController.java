package com.tanhua.server.controller;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.RecommendUserVo;
import com.tanhua.server.service.RecommendUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tanhua")
public class RecommendUserController {


    @Autowired
    private RecommendUserService recommendUserService;

    /**
     * 查询今日佳人
     * @return
     */
    @GetMapping("/todayBest")
    public ResponseEntity todayBest(){

        RecommendUserVo vo=recommendUserService.todayBest();
        return ResponseEntity.ok(vo);
    }


    /**
     * 首页好友推荐动态
     * @param queryParam
     * @return
     */
    @GetMapping("/recommendation")
    public ResponseEntity recommendList(RecommendUserQueryParam queryParam){
        PageResult<RecommendUserVo> pageResult=recommendUserService.recommendList(queryParam);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 佳人信息
     * @param userId
     * @return
     */
    @GetMapping("/{userId}/personalInfo")
    public ResponseEntity getPersonalInfo(@PathVariable Long userId){
       RecommendUserVo vo= recommendUserService.getPersonalInfo(userId);
       return ResponseEntity.ok(vo);
    }

    /**
     * 查看陌生人问题
     */
    @GetMapping("/strangerQuestions")
    public ResponseEntity strangerQuestions(Long userId){
        String question=recommendUserService.strangerQuestions(userId);
        return ResponseEntity.ok(question);
    }

    /**
     * 回复陌生人问题
     * @return
     */
    @PostMapping("/strangerQuestions")
    public ResponseEntity replyStrangerQuestions(@RequestBody Map<String,Object> paramMap){
        recommendUserService.replyStrangerQuestions(paramMap);
        return ResponseEntity.ok(null);
    }
}

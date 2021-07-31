package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.VideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/smallVideos")
public class VideosController {

    @Autowired
    private VideosService videosService;

    /**
     * 上传视频
     * @param videoThumbnail
     * @param videoFile
     * @return
     */
    @PostMapping
    public ResponseEntity smallVideos(MultipartFile videoThumbnail,MultipartFile videoFile){
        videosService.post(videoThumbnail,videoFile);
        return ResponseEntity.ok(null);
    }


    /**
     * 分页查询小视频列表
     * @return
     */
    @GetMapping
    public ResponseEntity findPage(@RequestParam(value = "page",defaultValue = "1")Long page,@RequestParam(value = "pagesize",defaultValue = "10")Long pageSize){
        PageResult<VideoVo> pageResult=videosService.findPage(page,pageSize);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 小视频添加关注
     * @param followUserId
     * @return
     */
    @PostMapping("/{followUserId}/userFocus")
    public ResponseEntity focusUser(@PathVariable Long followUserId){
        videosService.focusUser(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 小视频取消关注
     * @param followUserId
     * @return
     */
    @PostMapping("/{followUserId}/userUnFocus")
    public ResponseEntity userUnFocus(@PathVariable Long followUserId){
        videosService.userFocus(followUserId);
        return ResponseEntity.ok(null);
    }
}

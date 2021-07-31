package com.tanhua.server.controller;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.server.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告
 */
@RestController
@RequestMapping("/messages")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 发布公告
     * @return
     */
    @GetMapping("/announcements")
    public ResponseEntity announcements(@RequestParam(defaultValue = "1")int page,@RequestParam(defaultValue = "10")int pagesize){

        return announcementService.announcements(page,pagesize);
    }
}

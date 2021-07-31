package com.tanhua.server.service;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.AnnouncementApi;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementService {

    @Reference
    private AnnouncementApi announcementApi;


    /**
     * 发布公告
     * @param page
     * @param pagesize
     * @return
     */
    public ResponseEntity announcements(int page, int pagesize) {
        //1.调用api分页查询全部发布公告
        PageResult<Announcement> pageResult=announcementApi.findAll(page,pagesize);
        //2.获取到所有的公告对象
        List<Announcement> records = pageResult.getItems();

        //3. 把一个公告转换成vo对象
        List<AnnouncementVo> list=new ArrayList<>();

        for (Announcement record : records) {
            AnnouncementVo vo=new AnnouncementVo();
            BeanUtils.copyProperties(record,vo);
            if(record.getCreated()!=null){
                vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd").format(record.getCreated()));
            }
            list.add(vo);
        }

        //4.返回vo对象值
        PageResult resultVo=new PageResult(pageResult.getCounts(),pageResult.getPagesize(),pageResult.getPage(),list);
        // PageResult resultVo=new PageResult(pageResult.getPage(),pageResult.getPagesize(),pageResult.getCounts(),list);
        return ResponseEntity.ok(resultVo);
    }
}

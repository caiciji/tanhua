package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.AnnouncementMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class AnnouncementApiImpl implements AnnouncementApi {

    @Autowired
    private AnnouncementMapper announcementMapper;


    /**
     * 发布公告
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult<Announcement> findAll(int page, int pagesize) {
        Page<Announcement> pages=new Page<>(page,pagesize);

        IPage<Announcement> iPageInfo=announcementMapper.selectPage(pages,new QueryWrapper<>());

        PageResult<Announcement> pageResult=new PageResult<Announcement>(iPageInfo.getTotal(),iPageInfo.getSize(),iPageInfo.getPages(),iPageInfo.getCurrent(),iPageInfo.getRecords());

        return pageResult;
    }
}

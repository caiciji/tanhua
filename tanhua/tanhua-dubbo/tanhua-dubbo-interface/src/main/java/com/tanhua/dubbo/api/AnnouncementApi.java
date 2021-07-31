package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;

public interface AnnouncementApi {

    /**
     * 分页查询公告
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<Announcement> findAll(int page, int pagesize);
}

package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface VisitorApi {

    /**
     * 添加访客的测试数据
     * @param visitor
     */
    void add(Visitor visitor);

    /**
     * 查询登录用户的访客
     * @return
     */
    List<Visitor> findLast4Visitors(Long loginUserId);

    /**
     * 查询登陆用户谁看过我的访客信息
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageByUserId(Long userId, Long page, Long pageSize);
}

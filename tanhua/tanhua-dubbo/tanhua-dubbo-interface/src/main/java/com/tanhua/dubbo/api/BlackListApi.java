package com.tanhua.dubbo.api;

import com.tanhua.domain.vo.PageResult;

public interface BlackListApi {

    /**
     * 通过登录用户id，分页查询登录用户的黑名单列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageByUserId(Long userId, Long page, Long pageSize);


    /**
     * 移除黑名单
     * @param userId
     * @param blacklistById
     */
    void delete(Long userId, Long blacklistById);
}

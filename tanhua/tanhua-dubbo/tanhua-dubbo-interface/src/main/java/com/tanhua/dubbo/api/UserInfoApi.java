package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;

import java.util.List;

public interface UserInfoApi {


    /**
     * 新添加用户-填写信息
     * @param userInfo
     */
    void add(UserInfo userInfo);

    /**
     * 新用户-选取头像
     * @param userInfo
     */
    void update(UserInfo userInfo);

    /**
     * 询用户信息-获取个人信息
     * @param userId
     * @return
     */
    UserInfo findById(Long userId);

    /**
     * 批量查询作者信息
     * @param userIds
     * @return
     */
    List<UserInfo> findByBatchId(List<Long> userIds);

}

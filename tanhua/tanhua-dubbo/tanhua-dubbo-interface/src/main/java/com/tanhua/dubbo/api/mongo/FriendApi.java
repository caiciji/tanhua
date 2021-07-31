package com.tanhua.dubbo.api.mongo;


import com.tanhua.domain.vo.PageResult;

public interface FriendApi {

    /**
     * 添加联系人
     * @param loginUserId
     * @param friendId
     */
    void makeFriends(Long loginUserId, Long friendId);

    /**
     * 查询联系人
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long userId, Long page, Long pageSize);
}

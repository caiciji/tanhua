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

    /**
     * 查询互相喜欢统计
     * @param loginUserId
     * @return
     */
    Long countByUserId(Long loginUserId);

    /**
     * 查询互相喜欢的信息
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageWithScore(Long userId, Long page, Long pageSize);
}

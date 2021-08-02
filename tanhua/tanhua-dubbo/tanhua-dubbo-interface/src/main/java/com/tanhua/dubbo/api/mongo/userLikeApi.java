package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

public interface userLikeApi {
    /**
     * 查询我喜欢的统计
     * @param loginUserId
     * @return
     */
    Long loveCount(Long loginUserId);

    /**
     * 查询喜欢我(粉丝)统计
     * @param loginUserId
     * @return
     */
    Long fanCount(Long loginUserId);

    /**
     * 查询我喜欢的用户信息
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageOneSideLike(Long userId, Long page, Long pageSize);

    /**
     * 查询喜欢我(粉丝)的用户信息
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageFens(Long userId, Long page, Long pageSize);

    /**
     * 粉丝 - 喜欢
     * @param loginUserId
     * @param fansId
     * @return
     */
    boolean fansList(Long loginUserId, Long fansId);
}

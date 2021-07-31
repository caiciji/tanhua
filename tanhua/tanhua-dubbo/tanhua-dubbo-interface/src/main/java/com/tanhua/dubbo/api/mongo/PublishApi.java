package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;

public interface PublishApi {

    /**
     * 发布动态
     * @param publish
     */
    void add(Publish publish);

    /**
     * 用户id分页查询好友动态
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findFriendPublishByTimeline(Long userId, Long page, Long pageSize);

    /**
     * 分页查询推荐动态
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findRecommendPublish(Long loginUserId, Long page, Long pageSize);

    /**
     * 查询登录用户的时间线表，
     * @param page
     * @param pageSize
     * @param userId
     * @return
     */
    PageResult findMyPublishList(Long page, Long pageSize, Long userId);

    /**
     * 查询单条动态
     * @param publishId
     * @return
     */
    Publish findById(String publishId);
}

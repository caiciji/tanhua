package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

public interface VideosApi {

    /**
     * 上传视频
     * @param video
     */
    void add(Video video);

    /**
     * 分页查询小视频列表
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long page, Long pageSize);

    /**
     * 小视频添加关注
     * @param followUser
     */
    void focusUser(FollowUser followUser);

    /**
     * 小视频取消关注
     * @param followUser
     */
    void userFocus(FollowUser followUser);
}

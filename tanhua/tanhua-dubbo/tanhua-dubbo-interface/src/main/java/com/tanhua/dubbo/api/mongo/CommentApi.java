package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

public interface CommentApi {

    /**
     * 动态 点赞
     * @param comment
     * @return
     */
    long save(Comment comment);

    /**
     * 删除 点赞
     * @param comment
     * @return
     */
    long remove(Comment comment);

    /**
     * 通过动态id分页查询评论列表
     * @param movementId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(String movementId, Long page, Long pageSize);

    /**
     * 对评论 点赞
     * @param comment
     * @return
     */
    long likeComment(Comment comment);

    /**
     *  对评论 取消点赞
     * @param comment
     * @return
     */
    long dislikeComment(Comment comment);

    /**
     * 分页查询点赞列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageUserId(Long userId,int commentType, Long page, Long pageSize);
}

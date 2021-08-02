package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

public interface RecommendUserApi {

    /**
     * 通过登录id查询今日佳人
     * @param loginUserId
     * @return
     */
    RecommendUser todaybest(Long loginUserId);

    /**
     * 分页查询推荐朋友信息
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult fingPage(Long page, Long pagesize, Long userId);

    /**
     * 佳人信息
     * @param loginUserId
     * @param userId
     * @return
     */
    Double queryForScore(Long loginUserId, Long userId);

    /**
     * 根据用户id查询附近的人
     * @param loginUserId
     * @param distance
     * @return
     */
    List<UserLocationVo> searchNearBy(Long loginUserId, Long distance);
}

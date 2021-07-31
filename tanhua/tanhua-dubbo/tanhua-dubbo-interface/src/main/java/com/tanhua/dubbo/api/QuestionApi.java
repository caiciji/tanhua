package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Question;

public interface QuestionApi {

    /**
     * 通过用户id查询痛着设置
     * @param userId
     * @return
     */
    Question findByUserId(Long userId);

    /**
     * 保存陌生人问题
     * @param question
     */
    void save(Question question);
}

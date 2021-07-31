package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.BasePojo;
import com.tanhua.domain.db.Question;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class QuestionApiImpl extends BasePojo implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 通过用户id查询通用设置
     * @param userId
     * @return
     */
    @Override
    public Question findByUserId(Long userId) {
        QueryWrapper<Question> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return questionMapper.selectOne(queryWrapper);
    }

    /**
     * 保存陌生人问题
     * @param question
     */
    @Override
    public void save(Question question) {
        question.setCreated(new Date());
        question.setUpdated(new Date());
        //1.获取用户登录的id
        Question questionInDB = findByUserId(question.getUserId());
        if(null !=questionInDB){
            //存在则更新
            question.setId(questionInDB.getId());
            questionMapper.updateById(question);
        }else{
            //不存在则添加
            questionMapper.insert(question);
        }
    }
}

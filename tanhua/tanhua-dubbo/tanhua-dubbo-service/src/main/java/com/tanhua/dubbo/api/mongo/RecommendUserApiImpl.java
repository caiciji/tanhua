package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendUserApiImpl implements RecommendUserApi {


    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 今日佳人
     * @param loginUserId
     * @return
     */
    @Override
    public RecommendUser todaybest(Long loginUserId) {
        //1.查询条件
        Query query=new Query();
        //2.toUserId=loginUserId
        query.addCriteria(Criteria.where("toUseId").is(loginUserId));
        //3.缘分值降序
        query.with(Sort.by(Sort.Order.desc("score")));
        //4.只查询一个缘分值
        RecommendUser todayBest = mongoTemplate.findOne(query, RecommendUser.class);
        return todayBest;
    }

    /**
     * 分页查询首页推荐
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult fingPage(Long page, Long pagesize, Long userId) {
        //1.构建条件查询
        Query query=new Query(Criteria.where("toUserId").is(userId));
        //2.统计总数
        long total = mongoTemplate.count(query, RecommendUser.class);
        //3.总数>0，需要查结果集
        List<RecommendUser> recommendUserList=new ArrayList<>();
        if(total>0) {
            //4.设置分页
            query.skip((page+1)*pagesize).limit(pagesize.intValue());
            //5.分页查询,得到结果集
            recommendUserList = mongoTemplate.find(query, RecommendUser.class);
        }
        //6.构建分页返回
        return PageResult.pageResult(page,pagesize,recommendUserList,total);
    }

    /**
     * 通过登陆用户与佳人id查询两者的缘分值
     * @param loginUserId
     * @param userId
     * @return
     */
    @Override
    public Double queryForScore(Long loginUserId, Long userId) {
        //1.构建条件
        Query query=new Query(Criteria.where("toUserId").is(loginUserId).and("userId").is(userId));
        //2. 查询佳人信息 【注意】在推荐好友表，必须存在佳人记录
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        if(null ==recommendUser){

            return RandomUtils.nextDouble(60,80);
        }
        return recommendUser.getScore();
    }
}

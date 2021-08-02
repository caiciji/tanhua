package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisitorApiImpl implements VisitorApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加访客的测试数据
     * @param visitor
     */
    @Override
    public void add(Visitor visitor) {
        mongoTemplate.insert(visitor);
    }

    /**
     * 查询登陆用户最近访客，取前4位
     * @Param loginUserId
     * @return
     */
    @Override
    public List<Visitor> findLast4Visitors(Long loginUserId) {
        //1.构建查询条件
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        //2.按时间降序
        query.with(Sort.by(Sort.Order.desc("date")));
        //3.设置分页，去前4条
        query.limit(4);
        List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
        //缺少缘分值 在RecommendUser toUserId=登录用用户id/ userId=访客id
        if(!CollectionUtils.isEmpty(visitorList)){
            //补全缘分值
            List<Long> visitorIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());
            //构建批量查询添加
            Query recommendUserQuery=new Query(Criteria.where("toUserId").is(loginUserId).and("userId").in(visitorIds));
            //登录用户于访客信息
            List<RecommendUser> recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
            Map<Long, Double> scoreMap =new HashMap<>();
            if(!CollectionUtils.isEmpty(scoreMap)){
                //转成map key=访客id，value=缘份值
                scoreMap = recommendUserList.stream().collect(Collectors.toMap(RecommendUser::getUserId, RecommendUser::getScore));
            }
            //设置访客的缘份值
            for (Visitor visitor : visitorList) {
                Double score = scoreMap.get(visitor.getVisitorUserId());
                if(null ==score){
                    score= RandomUtils.nextDouble(60,80);
                }
                visitor.setScore(score);
            }
        }
        //4.返回结果
        return visitorList;
    }

    /**
     * 查询登陆用户谁看过我的访客信息
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageByUserId(Long loginUserId, Long page, Long pageSize) {
        //1.构建查询条件
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        //2.查询总数
        long total = mongoTemplate.count(query, Visitor.class);
        //总数>0
        List<RecommendUser> recommendUserList=new ArrayList<>();
        if(total>0){
            //设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //设置按时间降序
            query.with(Sort.by(Sort.Order.desc("date")));
            //查询visitors的结果集
            List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
            //缺少缘分值， toUserId=登陆用户id,userId=访客id
            if(!CollectionUtils.isEmpty(visitorList)){
                //获取visitors的ids
                List<Long> visitorIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());
                //构建登录用户与访客的条件
                Query recommendUserQuery=new Query(Criteria.where("toUserId").is(loginUserId).and("userId").in(visitorIds));
                //查询结果集
               recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
            }
        }
        //放回PageResult
        return PageResult.pageResult(page,pageSize,recommendUserList,total);
    }
}

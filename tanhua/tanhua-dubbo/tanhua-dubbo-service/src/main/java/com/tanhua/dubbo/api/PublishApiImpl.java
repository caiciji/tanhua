package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.RecommendQuanzi;
import com.tanhua.domain.mongo.TimeLine;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.mongo.PublishApi;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublishApiImpl implements PublishApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 发布动态
     * @param publish
     */
    @Override
    public void add(Publish publish) {
        Long loginUserId = publish.getUserId();
        //1.生成动态id
        ObjectId publishId=new ObjectId();
        publish.setId(publishId);
        //2.生成动态时间戳
        long timeMillis = System.currentTimeMillis();
        publish.setCreated(timeMillis);
        //3.添加动态表
        mongoTemplate.insert(publish);
        //4.添加自己的时间线表记录
        TimeLine timeLine=new TimeLine();
        timeLine.setUserId(loginUserId);//作者id
        timeLine.setCreated(timeMillis);
        timeLine.setPublishId(publishId);
        //5.集合名
        String collectionName = "quanzi_time_line_" + loginUserId;
        mongoTemplate.insert(timeLine,collectionName);

        //6.查询好友
        //6.1：构建条件: userId=登陆用户id
        Query userFriendQuery=new Query(Criteria.where("userId").is(loginUserId));
        List<Friend> friendList = mongoTemplate.find(userFriendQuery, Friend.class);
        if(!CollectionUtils.isEmpty(friendList)) {
            for (Friend friend : friendList) {
                String friendName = "quanzi_time_line_" + friend.getFriendId();
                mongoTemplate.insert(timeLine, friendName);
            }
        }
    }

    /**
     * 用户id分页查询好友动态
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findFriendPublishByTimeline(Long userId, Long page, Long pageSize) {
        //1. 查询登录用户自己的时间线表，分页查询
        //1.1 构建条件 ，设置按创建的时间降序
        Query timeLineQuery=new Query();
        //1.2集合名称
        String timeLineCollectionName = "quanzi_time_line_" + userId;
        //1.3 统计总数
        long total = mongoTemplate.count(timeLineQuery, timeLineCollectionName);
        //1.4 总数大于0
        List<Publish> publishList=new ArrayList<>();
        if(total>0){
            //设置按创建的时间降序
            timeLineQuery.with(Sort.by(Sort.Order.desc("created")));
            //分页
            timeLineQuery.skip((page-1)*pageSize).limit(pageSize.intValue());
            //p2:返回值得泛型类型
            List<TimeLine> timeLines = mongoTemplate.find(timeLineQuery, TimeLine.class, timeLineCollectionName);
            //2. 把时间线表中的所有动态id取出
            List<ObjectId> publishIds = timeLines.stream().map(TimeLine::getPublishId).collect(Collectors.toList());
            //3.查询动态数据
            //3.1 构建动态表的检查条件
            Query publishQuery=new Query(Criteria.where("id").in(publishIds));
            //3.2 通过批量id查询动态数据
          publishList = mongoTemplate.find(publishQuery, Publish.class);
        }
        //4. 构建PageResult返回
        return PageResult.pageResult(page,pageSize,publishList,total);
    }

    /**
     * 分页查询推荐动态
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findRecommendPublish(Long loginUserId, Long page, Long pageSize) {
        //1. 构建查询条件
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        //2. 统计总数
        long total = mongoTemplate.count(query, RecommendQuanzi.class);
        //3. 总数 >0
        List<Publish> publishList=new ArrayList<>();
        if(total>0){
            //4. 按时间降序，分页
            query.with(Sort.by(Sort.Order.desc("created")));
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //5. 推荐动态集合
            List<RecommendQuanzi> recommendQuanzis = mongoTemplate.find(query, RecommendQuanzi.class);

            //6. 取所有动态的id
            List<ObjectId> publishIds = recommendQuanzis.stream().map(RecommendQuanzi::getPublishId).collect(Collectors.toList());

            //7. 构建动态查询条件，ids集合
            Query publishQuery=new Query(Criteria.where("id").in(publishIds));
            //8. 皮脸查询动态
             publishList = mongoTemplate.find(publishQuery, Publish.class);
        }
        //9. 构建返回的PageResult
        return PageResult.pageResult(page,pageSize,publishList,total);
    }

    @Override
    public PageResult findMyPublishList(Long page, Long pageSize, Long userId) {
        //1.构建查询条件
        Query timeLineQuery=new Query(Criteria.where("userId").is(userId));
        //2.创建点赞表
        String timeLineCollectionName="quanzi_time_line_"+userId;
        //3.查询总数
        long total = mongoTemplate.count(timeLineQuery, timeLineCollectionName);
        //3.1 总数>0
        List<Publish> publishList=new ArrayList<>();
        if(total>0){
            //1.按时间降序
            timeLineQuery.with(Sort.by(Sort.Order.desc("created")));
            //2.设置分页
            timeLineQuery.skip((page-1)*pageSize).limit(pageSize.intValue());

            List<TimeLine> timeLineList = mongoTemplate.find(timeLineQuery, TimeLine.class, timeLineCollectionName);

            //4。把作者的全部取出
            List<ObjectId> publishIds = timeLineList.stream().map(TimeLine::getPublishId).collect(Collectors.toList());
            //5. 构建动态的查询条件
            Query publishQuery=new Query(Criteria.where("id").is(publishIds));
            //6.按时间降序
            publishQuery.with(Sort.by(Sort.Order.desc("created")));
            //7.查询
            publishList = mongoTemplate.find(publishQuery, Publish.class);
        }
        return PageResult.pageResult(page,pageSize,publishList,total);
    }

    /**
     * 查询单条动态
     * @param publishId
     * @return
     */
    @Override
    public Publish findById(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId),Publish.class);
    }
}

package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 交友
     * @param loginUserId
     * @param friendId
     */
    @Override
    public void makeFriends(Long loginUserId, Long friendId) {
        //1.添加好友 (登录用户 与佳人)
        long timeMillis = System.currentTimeMillis();
        Friend friend=new Friend();
        friend.setUserId(loginUserId);
        friend.setFriendId(friendId);
        friend.setCreated(timeMillis);
        //2.构建好友的条件
        Query query=new Query(Criteria.where("userId").is(loginUserId).and("friendId").is(friendId));
        //判断是否存在好友关系
        if (!mongoTemplate.exists(query, Friend.class)) {

            //添加好友
            mongoTemplate.insert(friend);
        }
        //3.构建好友的条件
        Query friendQuery=new Query(Criteria.where("userId").is(friendId).and("friendId").is(loginUserId));
        if(!mongoTemplate.exists(query,Friend.class)){
            friend=new Friend();
            friend.setUserId(friendId);
            friend.setFriendId(loginUserId);
            friend.setCreated(timeMillis);

            mongoTemplate.insert(friend);
        }
    }

    /**
     * 查看所有联系人
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Long userId, Long page, Long pageSize) {
        //1.构建查询条件
        Query query=new Query(Criteria.where("userId").is(userId));
        //2.统计总数
        long total = mongoTemplate.count(query, Friend.class);
        //3.总数>0
        List<Friend> friendList=new ArrayList<>();
        if(total>0){
            //设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //查询结果集
            friendList = mongoTemplate.find(query, Friend.class);
        }
        //4.返回pageResult
        return PageResult.pageResult(page,pageSize,friendList,total);
    }

    /**
     * 查询互相喜欢统计
     * @param loginUserId
     * @return
     */
    @Override
    public Long countByUserId(Long loginUserId) {
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        return mongoTemplate.count(query,Friend.class);
    }

    /**
     * 查询互相喜欢的用户信息
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageWithScore(Long loginUserId, Long page, Long pageSize) {
        //1.查询user_friend
        //构建条件
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        //统计总数
        long total = mongoTemplate.count(query, Friend.class);
        //总数>0
        List<RecommendUser> recommendUserList=new ArrayList<>();
        if(total>0){
            //设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //设值降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //查询登录用户与好友的缘分值
            //查询结果集
            List<Friend> friendList = mongoTemplate.find(query, Friend.class);
            //查询登录用户与好友的缘分值ids
            List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
            //构建推荐表的条件
            Query recommendUserQuery=new Query(Criteria.where("toUserId").is(loginUserId).and("userId").in(friendIds));
            //查询
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        //返回PageResult
        return PageResult.pageResult(page,pageSize,recommendUserList,total);
    }
}

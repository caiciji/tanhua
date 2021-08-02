package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.db.User;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Reference;
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
public class userLikeApiImpl implements userLikeApi {

    @Reference
    private FriendApi friendApi;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询我喜欢的统计
     * @param loginUserId
     * @return
     */
    @Override
    public Long loveCount(Long loginUserId) {
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 查询喜欢我(粉丝)统计
     * @param loginUserId
     * @return
     */
    @Override
    public Long fanCount(Long loginUserId) {
        Query query=new Query(Criteria.where("likeUserId").is(loginUserId));
        return mongoTemplate.count(query,UserLike.class);
    }

    /**
     * 查询我喜欢的用户信息
     *  1 互相关注
     *  2 我关注
     *  3 粉丝
     *  4 谁看过我
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageOneSideLike(Long loginUserId, Long page, Long pageSize) {
        //1.构建userLike的条件
        Query query=new Query(Criteria.where("userId").is(loginUserId));
        //2.获取统计总数
        long total = mongoTemplate.count(query, UserLike.class);
        //总数>0
        List<RecommendUser> recommendUserList=new ArrayList<>();
        if(total>0){
            //设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //设置按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //查询与佳人的的信息
            //获取佳人的结果集
            List<UserLike> userLikeList = mongoTemplate.find(query, UserLike.class);
            //获取佳人的ids
            List<Long> userLikeIds = userLikeList.stream().map(UserLike::getLikeUserId).collect(Collectors.toList());
            //构建推荐用户的条件
           Query recommendUserQuery=new Query(Criteria.where("toUserId").is(loginUserId).and("userId").in(userLikeIds));
           //查询推荐用户
             recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        //设置pageResult返回
        return PageResult.pageResult(page,pageSize,recommendUserList,total);
    }

    /**
     *查询喜欢我(粉丝)的用户信息
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageFens(Long loginUserId, Long page, Long pageSize) {
        //1.构建userLike的条件
        Query query=new Query(Criteria.where("likeUserId").is(loginUserId));
        //2.获取统计总数
        long total = mongoTemplate.count(query, UserLike.class);
        //总数>0
        List<RecommendUser> recommendUserList=new ArrayList<>();
        if(total>0){
            //设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //设置按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //查询推荐用户的结果集
            List<UserLike> fanUserList = mongoTemplate.find(query, UserLike.class);
            //查询粉丝的ids
            List<Long> fanIds = fanUserList.stream().map(UserLike::getUserId).collect(Collectors.toList());
            //构建粉丝的条件
            Query recommendUserQuery=new Query(Criteria.where("toUserId").is(loginUserId).and("userId").in(fanIds));
            //查询粉丝用户信息结果集
             recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        //返回PageResult
        return PageResult.pageResult(page,pageSize,recommendUserList,total);
    }

    /**
     * 粉丝 - 喜欢
     * @param loginUserId
     * @param fansId
     * @return
     */
    @Override
    public boolean fansList(Long loginUserId, Long fansId) {
        //1.先构建粉丝是否喜欢我的条件
        Query query=new Query(Criteria.where("likeUserId").is(loginUserId).and("userId").is(fansId));
        if(mongoTemplate.exists(query,UserLike.class)){
            //1.如果粉丝喜欢我，则删除这条记录，添加为好友
            mongoTemplate.remove(query,UserLike.class);
            //添加互为好友
            friendApi.makeFriends(loginUserId,fansId);
            return true;
        }else{
            //3.如果她不喜欢，则添加为我喜欢她
            UserLike userLike=new UserLike();
            userLike.setUserId(loginUserId);
            userLike.setLikeUserId(fansId);
            userLike.setCreated(System.currentTimeMillis());
            mongoTemplate.insert(userLike);
        }
        return false;
    }
}

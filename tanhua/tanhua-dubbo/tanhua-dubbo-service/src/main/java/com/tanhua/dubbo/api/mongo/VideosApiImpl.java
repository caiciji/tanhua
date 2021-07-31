package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideosApiImpl implements VideosApi {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 上传视频
     * @param video
     */
    @Override
    public void add(Video video) {
        mongoTemplate.insert(video);
    }

    /**
     * 分页查询小视频列表
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pageSize) {
        //1.构建查询条件
        Query query=new Query();
        //2. 统计总数
        long total = mongoTemplate.count(query, Video.class);
        //3. 总数>0
        List<Video> videoList=new ArrayList<>();
        if(total>0){
            //设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //分页查询
            videoList = mongoTemplate.find(query, Video.class);
        }
        //4. 返回分页查询pageResult
        return PageResult.pageResult(page,pageSize,videoList,total);
    }

    /**
     * 小视频添加关注
     * @param followUser
     */
    @Override
    public void focusUser(FollowUser followUser) {
        followUser.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(followUser);
    }

    /**
     * 小视频取消关注
     * @param followUser
     */
    @Override
    public void userFocus(FollowUser followUser) {
        //1.构建删除的条件
        Query query=new Query(Criteria.where("UserId").and("followUser").is(followUser.getFollowUserId()));

        mongoTemplate.remove(query,FollowUser.class);
    }
}

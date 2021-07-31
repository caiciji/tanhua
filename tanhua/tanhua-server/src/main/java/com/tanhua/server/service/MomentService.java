package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MomentService {

    @Reference
    private PublishApi publishApi;

    @Reference
    private UserInfoApi userInfoApi;


    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布动态
     * @param publishVo
     * @param imageContent
     */
    public void postMapping(PublishVo publishVo, MultipartFile[] imageContent) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();
        //2.把图片上传到阿里，收集url
        List<String> medias=new ArrayList<>();
        if(null !=imageContent){

            try {
                for (MultipartFile image : imageContent) {
                    String url = ossTemplate.upload(image.getOriginalFilename(), image.getInputStream());
                    medias.add(url);
                }
            } catch (IOException e) {
                // e.printStackTrace();
                log.error("上传图片到阿里失败",e);
                throw new TanHuaException("上传图片失败");
            }
        }
        //3.vo转成pojo
        Publish publish=new Publish();
        BeanUtils.copyProperties(publishVo,publish);
        //4.调用动态的作者
        publish.setUserId(loginUserId);
        publish.setState(0);//0：未审核
        //5.设置图片
        publish.setMedias(medias);
        publish.setSeeType(0);//公开
        publish.setLocationName(publishVo.getLocation());
        //6.调用api
        publishApi.add(publish);
    }

    /**
     * 好友 动态
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MomentVo> queryFriendPublishList(Long page, Long pageSize) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 通过登陆用户id查询自己的时间线表与动态的数据
        PageResult pageResult = publishApi.findFriendPublishByTimeline(loginUserId, page,pageSize);
        List<Publish> publishList = pageResult.getItems();
        //3. 补全作者用户信息
        if(!CollectionUtils.isEmpty(publishList)) {
            //3.1 取出所有作者ids
            List<Long> userIds = publishList.stream().map(Publish::getUserId).collect(Collectors.toList());
            //3.2 批量查询作者信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            //3.3 把作者是信息转成map<Key=作者id, value=作者userInfo>
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //4. 把动态转成MomentVO
            List<MomentVo> voList = publishList.stream().map(publish -> {
                MomentVo vo = new MomentVo();
                BeanUtils.copyProperties(publish, vo);
                // toArray(new String[0]) 把集合转成数组
                vo.setImageContent(publish.getMedias().toArray(new String[0]));
                vo.setDistance("500米");
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                // 作者信息
                UserInfo userInfo = userInfoMap.get(publish.getUserId());
                BeanUtils.copyProperties(userInfo,vo);
                vo.setTags(StringUtils.split(userInfo.getTags(),','));
                // 动态的id
                vo.setId(publish.getId().toHexString());
                String key = "publish_like_" + loginUserId + "_" + vo.getId();
                if(redisTemplate.hasKey(key)){
                    // 点赞过了
                    vo.setHasLiked(1);// 1: 点赞过了
                }
                return vo;
            }).collect(Collectors.toList());
            //5. 设置回到pageResult
            pageResult.setItems(voList);
        }
        //6. 返回
        return pageResult;
        }

    /**
     * 推荐动态
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MomentVo> queryRecommendPublishList(Long page, Long pageSize) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 通过登陆用户id查询自己的时间线表与动态的数据
        PageResult pageResult = publishApi.findRecommendPublish(loginUserId, page,pageSize);
        List<Publish> publishList = pageResult.getItems();
        //3. 补全作者用户信息
        if(!CollectionUtils.isEmpty(publishList)) {
            //3.1 取出所有作者ids
            List<Long> userIds = publishList.stream().map(Publish::getUserId).collect(Collectors.toList());
            //3.2 批量查询作者信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            //3.3 把作者是信息转成map<Key=作者id, value=作者userInfo>
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //4. 把动态转成MementVO
            List<MomentVo> voList = publishList.stream().map(publish -> {
                MomentVo vo = new MomentVo();
                BeanUtils.copyProperties(publish, vo);
                // toArray(new String[0]) 把集合转成数组
                vo.setImageContent(publish.getMedias().toArray(new String[0]));
                vo.setDistance("500米");
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                // 作者信息
                UserInfo userInfo = userInfoMap.get(publish.getUserId());
                BeanUtils.copyProperties(userInfo,vo);
                vo.setTags(StringUtils.split(userInfo.getTags(),','));
                // 动态的id
                vo.setId(publish.getId().toHexString());
                String key = "publish_like_" + loginUserId+"_" + vo.getId();
                if(redisTemplate.hasKey(key)){
                    vo.setHasLiked(1); // 点赞过了
                }
                // 是否喜欢处理
                key = "publish_love_" + loginUserId + "_" + vo.getId();
                if(redisTemplate.hasKey(key)){
                    // 喜欢过了
                    vo.setHasLoved(1);// 1: 喜欢过了
                }
                return vo;
            }).collect(Collectors.toList());
            //5. 设置回到pageResult
            pageResult.setItems(voList);
        }
        //6. 返回
        return pageResult;
    }


    /**
     * 查询用户动态
     * @param page
     * @param pageSize
     * @param userId
     * @return
     */
    public PageResult<MomentVo> queryMyPublishList(Long page, Long pageSize, Long userId) {
        //1.默认是登录用户id
        Long longUserId = UserHolder.getUserId();
        Long targetUserId=longUserId;
        if(null !=userId){
            //如果userId有值，就查看某个有用户动态
            targetUserId=userId;
        }
        //2.查询登录用户的时间线表，
       PageResult pageResult= publishApi.findMyPublishList(page,pageSize,targetUserId);
        List<Publish> publishList = pageResult.getItems();
        //3.补全用户信息
        if(!CollectionUtils.isEmpty(publishList)){
            //3.1,所有的作者都是登录用户
            UserInfo userInfo = userInfoApi.findById(targetUserId);
            //4.把动态转vo
            List<MomentVo> voList= publishList.stream().map(publish -> {
                MomentVo vo=new MomentVo();
                BeanUtils.copyProperties(userInfo,vo);
                //把toArray(new Object()) 把集合转成数组
                vo.setImageContent(publish.getMedias().toArray(new String[0]));
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                vo.setDistance("500米");

                BeanUtils.copyProperties(userInfo,vo);
                vo.setTags(StringUtils.split(userInfo.getTags(),','));
                //动态的id
                vo.setId(publish.getId().toHexString());

                String key="publish_like_"+longUserId+"_"+vo.getId();
                if(redisTemplate.hasKey(key)){
                    //点赞过了
                    vo.setHasLiked(1);//1:点赞
                }
                key="publish_love_"+longUserId+"_"+vo.getId();
                if(redisTemplate.hasKey(key)){
                    //喜欢过了
                    vo.setHasLoved(1);//3::喜欢
                }

                return vo;
            }).collect(Collectors.toList());
            //5.设置回到pageResult
            pageResult.setItems(voList);
        }
        //返回
        return pageResult;
    }

    /**
     * 通过 id 查询单条动态
     * @param publishId
     * @return
     */
    public MomentVo findById(String publishId) {
        //1.调用api查询单条动态
       Publish publish= publishApi.findById(publishId);
       //2. 取动态的作者id
        Long userId = publish.getUserId();
        //3. 查询作者的详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //4. 构建vo
        MomentVo vo=new MomentVo();
        BeanUtils.copyProperties(publish,vo);
        //toArray(new String[0])把集合转成数组
        vo.setImageContent(publish.getMedias().toArray(new String[0]));
        vo.setDistance("500米");
        vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));

        BeanUtils.copyProperties(userInfo,vo);
        vo.setTags(StringUtils.split(userInfo.getTags(),','));
        //动态的id
        vo.setId(publish.getId().toHexString());
        Long loginUserId = UserHolder.getUserId();
        String key = "publish_like_" + loginUserId + "_" + vo.getId();
        if(redisTemplate.hasKey(key)){
            // 点赞过了
            vo.setHasLiked(1);// 1: 点赞过了
        }
        // 是否喜欢处理
        key = "publish_love_" + loginUserId + "_" + vo.getId();
        if(redisTemplate.hasKey(key)){
            // 喜欢过了
            vo.setHasLoved(1);// 1: 喜欢过了
        }
        //7.返回vo
        return vo;
    }
}

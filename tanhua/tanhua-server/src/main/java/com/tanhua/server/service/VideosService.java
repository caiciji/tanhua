package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VideosApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideosService {


    @Reference
    private VideosApi videosApi;

    @Reference
    private UserInfoApi userInfoApi;


    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private RedisTemplate redisTemplate;

    public VideosService() {
    }

    /**
     * 上传视频
     * @param videoThumbnail
     * @param videoFile
     */
    //@CacheEvict(value = "videoList",allEntries = true)
    public void post(MultipartFile videoThumbnail, MultipartFile videoFile) {
        //1.上传封面图片到阿里oss,接收图片
        try {
            String url = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
            //2. 上传视频到fastDFS，接收视频
            String videoFileName = videoFile.getOriginalFilename();
            String videoExtension = videoFileName.substring(videoFileName.lastIndexOf(".") + 1);
            StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), videoExtension, null);
            String videoUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
            //3. 构建videos对象
            Video video=new Video();
            video.setVideoUrl(videoUrl);
            video.setUserId(UserHolder.getUserId());
            video.setPicUrl(url);
            video.setText("国一出品");
            //4. 调用api添加数据
            videosApi.add(video);
        } catch (IOException e) {
            log.error("上传封面图片失败",e);
            throw new TanHuaException("上传封面图片失败");
        }

    }

    /**
     * 分页查询小视频列表
     * @param page
     * @param pageSize
     * @return
     */
    //@Cacheable(value = "videoList",key = "#page +'_' + #pageSize")
    public PageResult findPage(Long page, Long pageSize) {
        //1.调用api查询
     PageResult pageResult= videosApi.findPage(page,pageSize);
        //2. 获取结果集
        List<Video>  videoList = pageResult.getItems();
        if(!CollectionUtils.isEmpty(videoList)){
            //3. 获取作者的ids
            List<Long> userIds = videoList.stream().map(Video::getUserId).collect(Collectors.toList());
            //4. 批量查询作者的信息List
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            //5. 把作者的信息转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //6. 小视频转成vo
            List<VideoVo> voList = videoList.stream().map(video -> {
                //7. 复制小视频信息
                VideoVo vo = new VideoVo();
                BeanUtils.copyProperties(video, vo);
                vo.setId(video.getId().toHexString());
                vo.setSignature(video.getText());
                vo.setCover(video.getPicUrl());
                //8. 复制作者信息
                UserInfo userInfo = userInfoMap.get(video.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                String key="follow_user_"+UserHolder.getUserId()+"_"+video.getUserId();
                if(redisTemplate.hasKey(key)){
                    vo.setHasFocus(1);//1: 已关注
                }
                return vo;
            }).collect(Collectors.toList());
            //9. 添加到pageResult
            pageResult.setItems(voList);
        }

        //10. 返回分页查询
        return pageResult;
    }

    /**
     * 小视频添加关注
     * @param followUserId
     */
    public void focusUser(Long followUserId) {
        //1.构建followUser对象
        FollowUser followUser=new FollowUser();
        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        //2.调用api添加关注
        videosApi.focusUser(followUser);
        //3. 存入redis登陆用户的标记
        String key="follow_user_"+UserHolder.getUserId()+"_"+followUserId;
        redisTemplate.opsForValue().set(key,1);
    }

    /**
     * 小视频取消关注
     * @param followUserId
     */
    public void userFocus(Long followUserId) {
        //1.构建followUser对象
        FollowUser followUser=new FollowUser();
        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        //2.调用api取消关注
        videosApi.userFocus(followUser);
        //3.删除redis中的key
        String key="follow_user_"+UserHolder.getUserId()+"_"+followUserId;
        redisTemplate.delete(key);
    }
}

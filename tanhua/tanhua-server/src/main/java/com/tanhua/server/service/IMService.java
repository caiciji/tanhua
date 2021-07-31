package com.tanhua.server.service;

import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IMService {

   @Reference
   private FriendApi friendApi;

   @Reference
   private UserInfoApi userInfoApi;

   @Reference
   private CommentApi commentApi;

   @Autowired
   private HuanXinTemplate huanXinTemplate;


    /**
     * 添加联系人
     * @param userId
     */
    public void makeFriends(Long userId) {
        //1.调用api添加登录用id和好友id
        friendApi.makeFriends(UserHolder.getUserId(),userId);
        //2. 在环信上添加，【注意】，这个id必须在环信存在，不然有可能404
        huanXinTemplate.makeFriends(UserHolder.getUserId(),userId);
    }

    /**
     * 查看联系人
     * @param page
     * @param pageSize
     * @param keyword
     * @return
     */
    public PageResult<ContactVo> queryContactsList(Long page, Long pageSize, String keyword) {
        //1.通过登录用户id查询联系人信息
       PageResult pageResult=friendApi.findPage(UserHolder.getUserId(),page,pageSize);
       //2.获取结果集
        List<Friend> friendList  = pageResult.getItems();
        //3.判断是否有结果集
        if(!CollectionUtils.isEmpty(friendList)){
            //4.取出所有好友的ids
            List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
            //5.查询所有好友的信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(friendIds);
            //6.转成vo
            List<ContactVo> voList = userInfoList.stream().map(userInfo -> {
                ContactVo vo = new ContactVo();
                //7.复制属性
                BeanUtils.copyProperties(userInfo, vo);
                vo.setUserId(userInfo.getId().toString());
                return vo;
            }).collect(Collectors.toList());
            //8。设置pageResult的返回值
            pageResult.setItems(voList);
        }
        //9.返回pageResult
        return pageResult;
    }

    /**
     * 分页查询列表 1(点赞),3（喜欢），2(评论)
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MessageVo> messageCommentList(int commentType,Long page, Long pageSize) {
        //1.通过登录用户id分页查询quanzi_comment
       PageResult pageResult= commentApi.findPageUserId(UserHolder.getUserId(),commentType,page,pageSize);
       //2.查询结果集
        List<Comment> commentList = pageResult.getItems();
        //判断是否有结果集
        if(!CollectionUtils.isEmpty(commentList)){
            //2.获取评论者所有用户id
            List<Long> commentIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());
            //3.批量查询评论者用户信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(commentIds);
            //把用户信息转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //4.把所有的评论者的用户转成vo
            List<MessageVo> voList = commentList.stream().map(comment -> {
                MessageVo vo = new MessageVo();
                //获取评论者的信息
                UserInfo userInfo = userInfoMap.get(comment.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                vo.setId(userInfo.getId().toString());
                vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                return vo;
            }).collect(Collectors.toList());
            pageResult.setItems(voList);
        }
        return pageResult;
    }


}

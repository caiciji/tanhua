package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 佳人业务处理
 */
@Service
public class RecommendUserService {

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 查询今日佳人
     * @return
     */
    public RecommendUserVo todayBest() {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();
        //2.调用api查询今日佳人
        RecommendUser todayBest= recommendUserApi.todaybest(loginUserId);

        //没有就推荐用户
        if(null ==todayBest){
            //使用客服
            todayBest =new RecommendUser();
            todayBest.setUserId(RandomUtils.nextLong(1,99));//1-99都是客服
            todayBest.setScore(RandomUtils.nextDouble(60,80));//缘分值是60-80
        }
        //3.通过id查询今日佳人详情
        UserInfo userInfo = userInfoApi.findById(todayBest.getUserId());
        //4.构建vo
        RecommendUserVo vo=new RecommendUserVo();
        //复制属性
        BeanUtils.copyProperties(userInfo,vo);
        //tags赋值
        vo.setTags(StringUtils.split(userInfo.getTags(), ','));
        //fateValue赋值
        vo.setFateValue(todayBest.getScore().longValue());

        //返回
        return vo;

    }

    /**
     * 推荐列表
     * @param queryParam
     * @return
     */
    public PageResult<RecommendUserVo> recommendList(RecommendUserQueryParam queryParam) {
        //1.根据token查询用户信息
        Long userId = UserHolder.getUserId();
       PageResult result= recommendUserApi.fingPage(queryParam.getPage(),queryParam.getPagesize(),userId);
        List<RecommendUser> records = (List<RecommendUser>)result.getItems();
        //2.如果未找到使用默认
        if(CollectionUtils.isEmpty(records)){
            result.setCounts(10l);
            result.setPages(1l);
            records=  defaultRecommend();
        }
        //3.转成vo
        List<RecommendUserVo> recommendUsers=new ArrayList<>();
        for (RecommendUser record : records) {
            RecommendUserVo vo=new RecommendUserVo();
            //补全用户信息
            UserInfo userInfo = this.userInfoApi.findById(record.getUserId());
            BeanUtils.copyProperties(userInfo,vo);
            vo.setFateValue(record.getScore().longValue());
            vo.setTags(StringUtils.split(userInfo.getTags(), ','));

            recommendUsers.add(vo);
        }
        //构建vo返回
        result.setItems(recommendUsers);
        return result;
    }


    //构造默认数据
    private List<RecommendUser> defaultRecommend() {
        String ids = "1,2,3,4,5,6,7,8,9,10";
        List<RecommendUser> records = new ArrayList<>();
        for (String id : ids.split(",")) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(Long.valueOf(id));
            recommendUser.setScore(RandomUtils.nextDouble(70, 98));
            records.add(recommendUser);
        }
        return records;
    }

    /**
     * 佳人信息
     * @param userId
     * @return
     */
    public RecommendUserVo getPersonalInfo(Long userId) {
        //1.查看当前登录用户的缘分值
        Long loginUserId = UserHolder.getUserId();
       Double score =recommendUserApi.queryForScore(loginUserId,userId);
        //2.查看佳人的详情信息
        UserInfo userInfo = userInfoApi.findById(userId);
        //3.转成vo
        RecommendUserVo vo=new RecommendUserVo();
        //4.复制属性
        BeanUtils.copyProperties(userInfo,vo);

        vo.setTags(StringUtils.split(userInfo.getTags(),','));

        vo.setFateValue(score.longValue());
        //5.返回值
        return vo;

    }

    /**
     * 查看陌生人问题
     * @param userId
     * @return
     */
    public String strangerQuestions(Long userId) {
        //1.调用api查询陌生人问题
        Question question=questionApi.findByUserId(userId);
        //2.判断有没有问题
        if(question==null){
            return "你喜欢我吗?";
        }
        //3.返回值
        return question.getTxt();
    }

    /**
     * 回复陌生人问题
     * @param paramMap
     */
    public void replyStrangerQuestions(Map<String, Object> paramMap) {
        //1.获取佳人id
        Integer userId = (Integer)paramMap.get("userId");
        //2.获取回复内容
        String reply = (String)paramMap.get("reply");
        //3.通过登录用户，获取nickname
        UserInfo userInfo = userInfoApi.findById(UserHolder.getUserId());
        String nickname = userInfo.getNickname();
        //4. 通过佳人id查询陌生人问题
        Question question = questionApi.findByUserId(userId.longValue());
        String strangerQuestion = question == null ? "你喜欢我吗?" : question.getTxt();
        //5. 构建环信内容
        Map<String,Object> mesMap=new HashMap<>();
        mesMap.put("userId", userId);
        mesMap.put("nickname", nickname);
        mesMap.put("strangerQuestion",strangerQuestion  );
        mesMap.put("reply", reply);

        //6.调用环信发送信息
        huanXinTemplate.sendMsg(userId.toString(),JSON.toJSONString(mesMap));
    }

    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> searchNearBy(String gender, Long distance) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();
        //2.根据用户id查询附近的人
       List<UserLocationVo> userLocationVoList= recommendUserApi.searchNearBy(loginUserId,distance);
        List<NearUserVo> voList=new ArrayList<>();
       //3.补全附近的人信息，过滤昵称
        if(!CollectionUtils.isEmpty(userLocationVoList)){
            List<Long> userLocationIds = userLocationVoList.stream().map(UserLocationVo::getUserId).collect(Collectors.toList());
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userLocationIds);
            //性别过滤并转vo，filter(保留)
            voList = userInfoList.stream().filter(userInfo -> userInfo.getGender().equals(gender)).map(userInfo -> {
                NearUserVo vo = new NearUserVo();
                BeanUtils.copyProperties(userInfo, vo);
                vo.setUserId(userInfo.getId());
                return vo;
            }).collect(Collectors.toList());
        }
        return voList;
    }
}

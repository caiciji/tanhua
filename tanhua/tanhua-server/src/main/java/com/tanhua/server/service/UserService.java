package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.dubbo.api.mongo.VisitorApi;
import com.tanhua.dubbo.api.mongo.userLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtils;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户管理业务层
 */
@Service
@Slf4j
public class UserService {

    @Reference
    private UserApi userApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private VisitorApi visitorApi;

    @Reference
    private com.tanhua.dubbo.api.mongo.userLikeApi userLikeApi;

    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;



    /**
     * 通过手机号码查询
     * @param phone
     * @return
     */
    public User findUser(String phone) {
      return userApi.findByMobile(phone);
    }

    /**
     * 添加用户
     * @param user
     */
    public void saveUser(User user) {
        userApi.save(user);
    }

    /**
     * 注册登录和发送验证码
     * @param phone
     */
    public void sendValidateCode(String phone) {
        //获取手机号码
        String key = redisValidateCodeKeyPrefix + phone;

        //再从手机号码获取redis中获取key中值
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);

        //打印日志
        log.info("codeInRedis: {},{}",codeInRedis,phone);

        //判断是否redis中有没有值,有就报错，
        if(null !=codeInRedis){
            //提示验证码未失效
            throw new TanHuaException(ErrorResult.duplicate());
        }

        //生成6位数的验证码
        String validateCode = "123456";/*RandomStringUtils.randomNumeric(6);

        //调用smsTemplate发送验证码
        Map<String,String> sendResult=smsTemplate.sendValidateCode(phone,validateCode);


        if(null !=sendResult){
            //发送验证码返回不为空，就报错
            throw new TanHuaException(ErrorResult.fail());
        }*/

        //验证码发送成功
        log.info("发送验证码成功:{},{}",validateCode,phone);

        //存入redis中有效期为10天p1：key,p2:value值 ,p3:数值，p4:天数
        redisTemplate.opsForValue().set(key,validateCode,10, TimeUnit.DAYS);
    }

    /**
     * 登录和验证码校验
     * @param phone
     * @param verificationCode
     * @return
     */
    public Map<String, Object> loginVerification(String phone, String verificationCode) {
        //从redis中获取手机号码
        String key = redisValidateCodeKeyPrefix + phone;

        String codeInRedis = (String) redisTemplate.opsForValue().get(key);

        //打印日志
        log.info("codeInRedis,verificationCode: {},{}",codeInRedis,verificationCode);

        //判断
        if(null==codeInRedis){

            throw new TanHuaException(ErrorResult.loginError());
        }

        if(!StringUtils.equals(codeInRedis,verificationCode)){
            throw new TanHuaException(ErrorResult.validateCodeError());
        }

        redisTemplate.delete(key);


        User user = userApi.findByMobile(phone);

        boolean isNew=false;
        if(null==user){
            user=new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md2Hex(phone.substring(5)));

            Long userId = userApi.save(user);

            user.setId(userId);


            //环信上注册
            huanXinTemplate.register(userId);
           isNew=true;
        }
        String token = jwtUtils.createJWT(phone, user.getId());

        String userJsonString = JSON.toJSONString(user);

        String tokenKey = "TOKEN_" + token;

        redisTemplate.opsForValue().set(tokenKey,userJsonString,7,TimeUnit.DAYS);

        Map<String,Object> result=new HashMap<>();

        result.put("token",token);

        result.put("isNew",isNew);

        return result;
    }

    /***
     * 新添加用户-填写信息
     * @param userInfoVo
     */
    public void loginReginfo(UserInfoVo userInfoVo) {
        //1.从redis获取用户信息
        User loginUser = UserHolder.getUser();
        //2.vo转换成userinfo对象
        UserInfo userInfo=new UserInfo();

        BeanUtils.copyProperties(userInfoVo,userInfo);

        userInfo.setAge(GetAgeUtils.getAge(userInfoVo.getBirthday()));

        userInfo.setId(loginUser.getId());

        userInfoApi.add(userInfo);

    }

    /**
     * 新用户-选取头像
     * @param headPhoto
     */
    public void uploadAvatar(MultipartFile headPhoto) {
        //1.从redis中获取用户信息
        User loginUser = UserHolder.getUser();
        //2.百度云人脸检测
        try {
            //2.1如果没有这个人脸就报错
            if(!faceTemplate.detect(headPhoto.getBytes())){
                throw new TanHuaException(ErrorResult.faceError());
            }
            //3.上传oos
            String filename = headPhoto.getOriginalFilename();
            String avatar = ossTemplate.upload(filename, headPhoto.getInputStream());

            //4.调用api选取头像
            UserInfo userInfo=new UserInfo();

            userInfo.setId(loginUser.getId());
            userInfo.setAvatar(avatar);

            userInfoApi.update(userInfo);
        } catch (IOException e) {
           log.info("上传头像失败",e);
           throw new TanHuaException("上传头像失败");
        }
    }

    /**
     * 查询用户信息-获取个人信息
     * @return
     */
    public UserInfoVo getLoginUserInfo() {
        //1.获取redis的用户信息的Id
        Long userId = UserHolder.getUserId();
        //2.
       UserInfo userInfo= userInfoApi.findById(userId);

       //3.
        UserInfoVo vo=new UserInfoVo();

        BeanUtils.copyProperties(userInfo,vo);

        //4
        vo.setAge(userInfo.getAge().toString());

        return vo;
    }

    /**
     * 更新用户
     * @param userInfoVo
     */
    public void updateUserInfo(UserInfoVo userInfoVo) {
        //1.从redis中获取用户信息的Id
        Long userId = UserHolder.getUserId();
        //2.vo转成userInfo
        UserInfo userInfo=new UserInfo();
        BeanUtils.copyProperties(userInfoVo,userInfo);

        userInfo.setAge(GetAgeUtils.getAge(userInfoVo.getBirthday()));

        userInfo.setId(userId);

        userInfoApi.update(userInfo);

    }

    /**
     * 更新头像
     * @param headPhoto
     */
    public void updateUserHeader(MultipartFile headPhoto) {
        //1.从redis中获取用户信息Id
        User loginUser = UserHolder.getUser();

        //2.百度云检测
        try {
            if(!faceTemplate.detect(headPhoto.getBytes())){
                throw new TanHuaException(ErrorResult.faceError());
            }
            //3.上传oos
            String filename = headPhoto.getOriginalFilename();
            String avatar = ossTemplate.upload(filename, headPhoto.getInputStream());

            //查询用户的信息，获取旧头像的地址
            UserInfo userInfo = userInfoApi.findById(loginUser.getId());
            String oldHeaderUlr = userInfo.getAvatar();

            //4.调用api选取头像
            UserInfo updateUserInfo=new UserInfo();
            updateUserInfo.setId(loginUser.getId());
            updateUserInfo.setAvatar(avatar);

            userInfoApi.update(updateUserInfo);
            //删除旧头像
            ossTemplate.deleteFile(oldHeaderUlr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     * @return
     */
    public CountsVo counts() {
        Long loginUserId = UserHolder.getUserId();
        //1.查询互相喜欢统计
        Long eachLoveCount=friendApi.countByUserId(loginUserId);
        //2. 查询我喜欢的统计
        Long loveCount= userLikeApi.loveCount(loginUserId);
        //3.查询喜欢我(粉丝)统计
        Long fanCount= userLikeApi.fanCount(loginUserId);
        //4.转成vo
        CountsVo countsVo=new CountsVo();
        countsVo.setEachLoveCount(eachLoveCount);
        countsVo.setLoveCount(loveCount);
        countsVo.setFanCount(fanCount);
        return countsVo;
    }

    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<FriendVo> queryUserLikeList(int type, Long page, Long pageSize) {
        //1.根据type调用api查询，返回统计数据
        PageResult pageResult=null;
        Boolean alreadyLove=false;
        switch (type){
            case 1:pageResult=friendApi.findPageWithScore(UserHolder.getUserId(),page,pageSize);alreadyLove=true;break;
            case 2:pageResult=userLikeApi.findPageOneSideLike(UserHolder.getUserId(),page,pageSize);alreadyLove=true;break;
            case 3:pageResult=userLikeApi.findPageFens(UserHolder.getUserId(),page,pageSize);break;
            case 4:pageResult=visitorApi.findPageByUserId(UserHolder.getUserId(),page,pageSize);break;
            default: throw new TanHuaException("参数不对");
        }
        //2.获取登陆用户集合
        List<RecommendUser> userList = pageResult.getItems();
        if(!CollectionUtils.isEmpty(userList)){
            List<Long> userIds = userList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());
            //3.批量查询用户信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            //4.转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));

            final boolean finalAlreadyLove=alreadyLove;
            List<FriendVo> voList = userList.stream().map(recommendUser -> {
                FriendVo vo = new FriendVo();
                //获取属性
                UserInfo userInfo = userInfoMap.get(recommendUser.getUserId());
                //复制属性
                BeanUtils.copyProperties(userInfo, vo);
                //设置缘分值
                vo.setMatchRate(recommendUser.getScore().intValue());
                vo.setAlreadyLove(finalAlreadyLove);
                return vo;
            }).collect(Collectors.toList());
          //设置pageResult
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 粉丝 - 喜欢
     * @param fansId
     */
    public void fansLike(Long fansId) {
        //1.获取登录用户id
        Long loginUserId = UserHolder.getUserId();
        //2.调用api，实现添加好友的动态
       boolean flag= userLikeApi.fansList(loginUserId,fansId);
       if(flag){
           //如果添加好友动态，环信也要添加好友
           huanXinTemplate.makeFriends(loginUserId,fansId);
       }
    }
}

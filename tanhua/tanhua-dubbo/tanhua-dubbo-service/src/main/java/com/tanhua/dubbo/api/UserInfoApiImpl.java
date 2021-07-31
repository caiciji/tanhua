package com.tanhua.dubbo.api;

import com.tanhua.domain.db.BasePojo;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoApiImpl extends BasePojo implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 新添加用户-填写信息
     * @param userInfo
     */
    @Override
    public void add(UserInfo userInfo) {

        userInfoMapper.insert(userInfo);
    }

    /**
     * 新用户-选取头像
     * @param userInfo
     */
    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 询用户信息-获取个人信息
     * @param userId
     * @return
     */
    @Override
    public UserInfo findById(Long userId) {
        return userInfoMapper.selectById(userId);
    }

    /**
     * 批量查询作者信息
     * @param userIds
     * @return
     */
    @Override
    public List<UserInfo> findByBatchId(List<Long> userIds) {
        return userInfoMapper.selectBatchIds(userIds);
    }



}

package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class UserApiImpl implements UserApi {

    @Autowired
    private UserMapper userMapper;

    /**
     * 通过手机号码查询
     * @param mobile
     * @return
     */
    @Override
    public User findByMobile(String mobile) {

        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("mobile",mobile);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 添加用户
     * @param user
     * @return
     */
    @Override
    public Long save(User user) {
        user.setCreated(new Date());
        user.setUpdated(new Date());
        userMapper.insert(user);
        return user.getId();
    }

    /**
     *  * 修改手机号码：3.保存
     * @param userId
     * @param phone
     */
    @Override
    public void updateMobile(Long userId, String phone) {
        User user=new User();
        user.setMobile(phone);
        user.setId(userId);
        userMapper.updateById(user);
    }
}

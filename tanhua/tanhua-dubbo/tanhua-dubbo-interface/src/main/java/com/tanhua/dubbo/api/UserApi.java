package com.tanhua.dubbo.api;


import com.tanhua.domain.db.User;

public interface UserApi {

    /**
     * 通过手机号码查询
     * @param phone
     * @return
     */
    User findByMobile(String phone);

    /**
     * 添加用户
     * @param user
     */
    Long save(User user);

    /**
     *  * 修改手机号码：3.保存
     * @param userId
     * @param phone
     */
    void updateMobile(Long userId, String phone);
}

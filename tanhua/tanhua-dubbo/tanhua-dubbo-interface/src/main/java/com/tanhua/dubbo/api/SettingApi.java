package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Settings;

public interface SettingApi {
    /**
     * 根据id查询当前用户登录的信息
     * @param id
     * @return
     */
    Settings findByUserId(Long id);

    /**
     * 保存通用设置
     * @param settings
     */
    void save(Settings settings);


}

package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.BasePojo;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.SettingsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class SettingApiImpl extends BasePojo implements SettingApi {

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 根据id查询当前登录时用户信息
     * @param userId
     * @return
     */
    @Override
    public Settings findByUserId(Long userId) {
        QueryWrapper<Settings> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return settingsMapper.selectOne(queryWrapper);
    }

    /**
     * 保存通用设置
     * @param settings
     */
    @Override
    public void save(Settings settings) {
        settings.setCreated(new Date());
        settings.setUpdated(new Date());
        //1.通过用户id查询
        Settings settingInDB = findByUserId(settings.getUserId());
        //2.存在，则更新
        if(null !=settingInDB){
            settings.setId(settingInDB.getId());
            //updateById update bale set 列=值(不为空) where id=settings.getId();
            settingsMapper.updateById(settings);
        }else {
            //3.不存在，则添加
            settingsMapper.insert(settings);
        }
    }




}

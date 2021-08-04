package com.tanhua.manage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogMapper {

    /**
     * 过去的7天活跃用户和过去的30天的活跃用户
     * @param _7daySandLast30days
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time > #{_7daySandLast30days}")
    Long countActiveUserAfterDate(String _7daySandLast30days);
}

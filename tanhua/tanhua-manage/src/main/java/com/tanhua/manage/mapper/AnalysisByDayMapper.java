package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.vo.DataPointVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface AnalysisByDayMapper extends BaseMapper<AnalysisByDay> {

    /**
     * //1.查询累计用户
     * @return
     */
    @Select("select sum(num_registered) from tb_analysis_by_day")
    Long totalUserCount();

    /**
     * 查询今天和昨天的数据
     * @param today
     * @return
     */
    @Select("select num_registered, num_active, num_login from tb_analysis_by_day where record_date=#{today}")
    AnalysisByDay findByDate(String today);

    /**
     * 查询新增、活跃用户、次日留存率
     *  #{} ${} 两者的区别
     *   ? 点位符，prepared预编译，sql发给数据库
     *   ${} sql拼接 防止 sql注入
     * @param thisYearStartDate
     * @param thisYearSEndDate
     * @param column
     * @return
     */
    @Select("Select date_format(record_date,'%Y-%m-%d') title, ${column}  amount From tb_analysis_by_day where record_date " +
            " between #{thisYearStartDate} and #{thisYearSEndDate}")
    List<DataPointVo> findBetweenDate(@Param("thisYearStartDate")String thisYearStartDate,@Param("thisYearStartDate") String thisYearSEndDate,@Param("column")String column);
}
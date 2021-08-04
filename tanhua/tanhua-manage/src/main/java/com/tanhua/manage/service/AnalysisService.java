package com.tanhua.manage.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AnalysisService {

    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;

    @Autowired
    private LogMapper logMapper;


    /**
     * 概要统计信息
     * @return
     */
    public AnalysisSummaryVo symmary() {
        AnalysisSummaryVo vo=new AnalysisSummaryVo();
        Date todayDate=new Date();
        //1.查询累计用户
        Long cumulativeUsers=  analysisByDayMapper.totalUserCount();
        vo.setCumulativeUsers(cumulativeUsers);
         //2过去的七天日期，，30天日期
        String last7Date = DateUtil.offsetDay(todayDate, -7).toDateStr();
        String last30Date = DateUtil.offsetDay(todayDate, -30).toDateStr();
        //查询过去的7天活跃用户
        Long activePassWeek= logMapper.countActiveUserAfterDate(last7Date);
        vo.setActivePassWeek(activePassWeek);
        // 过去30天活跃用户
        Long activePassMonth= logMapper.countActiveUserAfterDate(last30Date);
        vo.setActivePassMonth(activePassMonth);
        //3.查询今天的数据
        String today = DateUtil.today();
        AnalysisByDay todayData= analysisByDayMapper.findByDate(today);
        vo.setNewUsersToday(todayData.getNumRegistered());
        vo.setActiveUsersToday(todayData.getNumActive());
        vo.setLoginTimesToday(todayData.getNumLogin());
        //4.查询昨天的数据
        String yesterday = DateUtil.yesterday().toDateStr();
        AnalysisByDay yesterdayData=analysisByDayMapper.findByDate(yesterday);
        //5.查询计算环比
        vo.setNewUsersTodayRate(ComputeUtil.computeRate(todayData.getNumRegistered(),yesterdayData.getNumRegistered()));

        vo.setLoginTimesTodayRate(ComputeUtil.computeRate(todayData.getNumLogin(),yesterdayData.getNumLogin()));

        vo.setActiveUsersTodayRate(ComputeUtil.computeRate(todayData.getNumActive(),yesterdayData.getNumActive()));
        return vo;
    }

    /**
     * 查询新增、活跃用户、次日留存率
     * @param sd
     * @param ed
     * @param type
     * @return
     */
    public AnalysisUsersVo getUsersCount(Long sd, Long ed, Integer type) {
        String column="";//要查询的列名
        //根据类型来查询列名
        switch (type){
            case 101:column="num_login";break;
            case 102:column="num_active";break;
            case 103:column="num_registered";break;
            default: throw new BuilderException("查询格式不全");
        }
        //今年的开始日期
       String thisYearStartDate=DateUtil.date(sd).toDateStr();
        //今年的结束日期
        String thisYearSEndDate=DateUtil.date(ed).toDateStr();
        //查询今年的数据
      List<DataPointVo> thisYearData = analysisByDayMapper.findBetweenDate(thisYearStartDate,thisYearSEndDate,column);
      //去年的开始日期
        String lastYearStartDate=DateUtil.date(sd).offset(DateField.YEAR,-1).toDateStr();
        //去年的结束日期
        String lastYearEndDate=DateUtil.date(ed).offset(DateField.YEAR,-1).toDateStr();

        //查询去年的数据
        List<DataPointVo> lastsYearData=analysisByDayMapper.findBetweenDate(lastYearStartDate,lastYearEndDate,column);

        //构建vo对象返回
        AnalysisUsersVo vo=new AnalysisUsersVo();
        vo.setThisYear(thisYearData);
        vo.setLastYear(lastsYearData);
        return vo;
    }
}

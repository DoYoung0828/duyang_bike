package com.bike.record.dao;

import com.bike.record.entity.RideRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RideRecordMapper {

    //1、检查用户有没有未关闭的骑行记录
    RideRecord selectRecordNotClosed(long userId);

    //2、建立订单,记录开始骑行时间
    int insertSelective(RideRecord record);

    //3、结束订单,计算骑行时间存订单(数据库中查询该单车尚未完结的订单)
    RideRecord selectBikeRecordOnGoing(Long bikeNo);

    //4、骑行结束,修改骑行状态相关信息
    int updateByPrimaryKeySelective(RideRecord record);

    //5、查询骑行历史
    List<RideRecord> selectRideRecordPage(@Param("userId") long userId,
                                          @Param("lastId") Long lastId);

    int deleteByPrimaryKey(Long id);

    int insert(RideRecord record);


    RideRecord selectByPrimaryKey(Long id);


    int updateByPrimaryKey(RideRecord record);

}
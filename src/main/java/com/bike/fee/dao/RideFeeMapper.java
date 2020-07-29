package com.bike.fee.dao;

import com.bike.fee.entity.RideFee;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface RideFeeMapper {

    //1、查询计价信息
    RideFee selectBikeTypeFee(Byte type);

    int deleteByPrimaryKey(Long id);

    int insert(RideFee record);

    int insertSelective(RideFee record);

    RideFee selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RideFee record);

    int updateByPrimaryKey(RideFee record);

}
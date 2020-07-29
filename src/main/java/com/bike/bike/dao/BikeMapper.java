package com.bike.bike.dao;

import com.bike.bike.entity.Bike;
import com.bike.bike.entity.BikeNoGen;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BikeMapper {

    //1、利用数据库自增特性,返回唯一ID作为单车编号
    void generateBikeNo(BikeNoGen bikeNoGen);

    //2、生成单车
    int insertSelective(Bike record);

    //3、查询单车类型
    Bike selectByBikeNo(Long bikeNo);

    int deleteByPrimaryKey(Long id);

    int insert(Bike record);

    Bike selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Bike record);

    int updateByPrimaryKey(Bike record);


}
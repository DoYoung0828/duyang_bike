package com.bike.wallet.dao;

import com.bike.wallet.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletMapper {

    //1、检查用户钱包余额是否足够(大于一元)
    Wallet selectByUserId(long userId);

    //2、钱包扣费
    int updateByPrimaryKeySelective(Wallet record);

    int deleteByPrimaryKey(Long id);

    int insert(Wallet record);

    int insertSelective(Wallet record);

    Wallet selectByPrimaryKey(Long id);

    int updateByPrimaryKey(Wallet record);

}
package com.bike.user.dao;

import com.bike.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
//@Component
public interface UserMapper {

    /**
     * 1.1、根据手机号码查找用户
     */
    User selectByMobile(@Param("mobile") String mobile);

    /**
     * 1.2、用户不存在时,注册用户(返回自增主键,int类型)
     */
    int insertSelective(User record);

    /**
     * 2、修改用户昵称(头像URL)
     */
    int updateByPrimaryKeySelective(User record);

    /**
     * 4、根据userId获取用户信息(校验是否实名认证)
     */
    User selectByPrimaryKey(Long id);

    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int updateByPrimaryKey(User record);

}
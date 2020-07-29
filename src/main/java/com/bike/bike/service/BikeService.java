package com.bike.bike.service;

import com.bike.bike.entity.BikeLocation;
import com.bike.common.exception.MaMaBikeException;
import com.bike.user.entity.UserElement;

public interface BikeService {

    //1、创建单车
    void generateBike() throws MaMaBikeException;

    //3、解锁单车,准备骑行
    void unLockBike(UserElement currentUser, Long bikeNo)throws MaMaBikeException;

    //4、锁车,骑行结束
    void lockBike(BikeLocation bikeLocation)throws MaMaBikeException;

    //5、上报停车坐标
    void reportLocation(BikeLocation bikeLocation)throws MaMaBikeException;

}

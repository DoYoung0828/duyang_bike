package com.bike.record.service;


import com.bike.common.exception.MaMaBikeException;
import com.bike.record.entity.RideRecord;

import java.util.List;

public interface RideRecordService {

    //1、查询骑行历史
    List<RideRecord> listRideRecord(long userId, Long lastId) throws MaMaBikeException;

}

package com.bike.record.service;

import com.bike.common.exception.MaMaBikeException;
import com.bike.record.dao.RideRecordMapper;
import com.bike.record.entity.RideRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RideRecordServiceImpl implements RideRecordService{

    @Autowired
    private RideRecordMapper rideRecordMapper;

    @Override
    public List<RideRecord> listRideRecord(long userId, Long lastId) throws MaMaBikeException {
        List<RideRecord> list = rideRecordMapper.selectRideRecordPage(userId,lastId);
        return list;
    }

}

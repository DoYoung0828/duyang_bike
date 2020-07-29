package com.bike.bike.service;

import com.alibaba.fastjson.JSONObject;
import com.bike.bike.dao.BikeMapper;
import com.bike.bike.entity.Bike;
import com.bike.bike.entity.BikeLocation;
import com.bike.bike.entity.BikeNoGen;
import com.bike.common.exception.MaMaBikeException;
import com.bike.common.utils.BaiduPushUtil;
import com.bike.common.utils.DateUtil;
import com.bike.common.utils.RandomNumberCode;
import com.bike.fee.dao.RideFeeMapper;
import com.bike.fee.entity.RideFee;
import com.bike.record.dao.RideRecordMapper;
import com.bike.record.entity.RideRecord;
import com.bike.user.dao.UserMapper;
import com.bike.user.entity.User;
import com.bike.user.entity.UserElement;
import com.bike.wallet.dao.WalletMapper;
import com.bike.wallet.entity.Wallet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BikeServiceImpl implements BikeService {

    private static final Byte NOT_VERYFY = 1;//未认证
    private static final Object BIKE_UNLOCK = 2;//单车解锁
    private static final Object BIKE_LOCK = 1;//单车锁定
    private static final Byte RIDE_END = 2;//骑行结束

    @Autowired
    private BikeMapper bikeMapper;//单车模块
    @Autowired
    private UserMapper userMapper;//用户模块
    @Autowired
    private MongoTemplate mongoTemplate;//mongodb操作
    @Autowired
    private RideRecordMapper rideRecordMapper;//骑行记录模块
    @Autowired
    private WalletMapper walletMapper;//钱包模块
    @Autowired
    private RideFeeMapper feeMapper;//计价信息模块

    @Transactional//事务(也可以添加到类上,表示该类所有的公共方法具有相同的事务属性)
    @Override
    public void generateBike() throws MaMaBikeException {
        //利用数据库自增特性,返回唯一ID作为单车编号
        BikeNoGen bikeNoGen = new BikeNoGen();
        bikeMapper.generateBikeNo(bikeNoGen);
        //生成单车
        Bike bike = new Bike();
        bike.setType((byte) 2);
        bike.setNumber(bikeNoGen.getAutoIncNo());
        bikeMapper.insertSelective(bike);
    }

    @Override
    @Transactional//mongodb修改单车状态,mysql修改金额,要求包装事务(实际是没有好的解决办法,不能保证)
    public void unLockBike(UserElement currentUser, Long bikeNo) throws MaMaBikeException {
        try {
            //检查用户是否已经认证(实名认证没,押金交了没)
            User user = userMapper.selectByPrimaryKey(currentUser.getUserId());
            if (user.getVerifyFlag() == NOT_VERYFY) {
                throw new MaMaBikeException("用户尚未认证");
            }
            //检查用户有没有未关闭的骑行记录
            RideRecord record = rideRecordMapper.selectRecordNotClosed(currentUser.getUserId());
            if (record != null) {
                throw new MaMaBikeException("存在未关闭骑行订单");
            }
            //检查用户钱包余额是否足够（大于一元）
            Wallet wallet = walletMapper.selectByUserId(currentUser.getUserId());
            if (wallet.getRemainSum().compareTo(new BigDecimal(1)) < 0) {
                throw new MaMaBikeException("余额不足");
            }
            //推送单车进行解锁
            JSONObject notification = new JSONObject();
            notification.put("unlock", "unlock");
            BaiduPushUtil.pushMsgToSingleDevice(currentUser, "{\"title\":\"TEST\",\"description\":\"Hello Baidu push!\"}");
            //推送如果可靠性比较差,可以采用单车端开锁后,主动ACK服务器,再修改相关状态的方式
            //修改mongoDB中单车状态
            Query query = Query.query(Criteria.where("bike_no").is(bikeNo));
            Update update = Update.update("status", BIKE_UNLOCK);
            mongoTemplate.updateFirst(query, update, "bike-position");
            //建立订单,记录开始骑行时间,同时骑行轨迹开始上报(另一个接口)
            RideRecord rideRecord = new RideRecord();
            rideRecord.setBikeNo(bikeNo);
            String recordNo = new Date().toString() + System.currentTimeMillis() + RandomNumberCode.randomNo();
            rideRecord.setRecordNo(recordNo);
            rideRecord.setStartTime(new Date());
            rideRecord.setUserid(currentUser.getUserId());
            rideRecordMapper.insertSelective(rideRecord);
        } catch (Exception e) {
            log.error("fail to un lock bike", e);
            throw new MaMaBikeException("解锁单车失败");
        }
    }

    @Override
    @Transactional
    public void lockBike(BikeLocation bikeLocation) throws MaMaBikeException {
        try {
            //结束订单,计算骑行时间存订单
            RideRecord record = rideRecordMapper.selectBikeRecordOnGoing(bikeLocation.getBikeNumber());
            if (record == null) {
                throw new MaMaBikeException("骑行记录不存在");
            }
            Long userid = record.getUserid();
            //查询单车类型(不同类型,价格不同)
            Bike bike = bikeMapper.selectByBikeNo(bikeLocation.getBikeNumber());
            if (bike == null) {
                throw new MaMaBikeException("单车不存在");
            }
            //查询计价信息
            RideFee fee = feeMapper.selectBikeTypeFee(bike.getType());
            if (fee == null) {
                throw new MaMaBikeException("计费信息异常");
            }
            BigDecimal cost = BigDecimal.ZERO;
            record.setEndTime(new Date());
            record.setStatus(RIDE_END);
            //计算骑行分钟
            Long min = DateUtil.getBetweenMin(new Date(), record.getStartTime());
            record.setRideTime(min.intValue());
            int minUnit = fee.getMinUnit();
            int intMin = min.intValue();
            //骑行时间处理,计算金额
            if (intMin / minUnit == 0) {
                //不足一个时间单位,按照一个时间单位算
                cost = fee.getFee();
            } else if (intMin % minUnit == 0) {
                //整除了时间单位,直接计费
                cost = fee.getFee().multiply(new BigDecimal(intMin / minUnit));
            } else if (intMin % minUnit != 0) {
                //不整除,+1,补足一个时间单位
                cost = fee.getFee().multiply(new BigDecimal((intMin / minUnit) + 1));
            }
            record.setRideCost(cost);
            //骑行结束,修改骑行状态相关信息
            rideRecordMapper.updateByPrimaryKeySelective(record);
            //钱包扣费
            Wallet wallet = walletMapper.selectByUserId(userid);
            wallet.setRemainSum(wallet.getRemainSum().subtract(cost));
            walletMapper.updateByPrimaryKeySelective(wallet);
            //修改mongoDB中单车状态为锁定
            Query query = Query.query(Criteria.where("bike_no").is(bikeLocation.getBikeNumber()));
            Update update = Update.update("status", BIKE_LOCK)
                    .set("location.coordinates", bikeLocation.getCoordinates());
            mongoTemplate.updateFirst(query, update, "bike-position");
        } catch (Exception e) {
            log.error("fail to lock bike", e);
            throw new MaMaBikeException("锁定单车失败");
        }
    }

    @Override
    public void reportLocation(BikeLocation bikeLocation) throws MaMaBikeException {
        //数据库中查询该单车尚未完结的订单
        RideRecord record = rideRecordMapper.selectBikeRecordOnGoing(bikeLocation.getBikeNumber());
        if (record == null) {
            throw new MaMaBikeException("骑行记录不存在");
        }
        //查询mongo中是否已经有骑行的坐标记录数据
        DBObject obj = mongoTemplate.getCollection("ride_contrail")
                .findOne(new BasicDBObject("record_no", record.getRecordNo()));
        //没有插入,已经存在,添加坐标
        if (obj == null) {
            //不存在时:
            List<BasicDBObject> list = new ArrayList();
            BasicDBObject temp = new BasicDBObject("loc", bikeLocation.getCoordinates());
            list.add(temp);
            BasicDBObject insertObj = new BasicDBObject("record_no", record.getRecordNo())
                    .append("bike_no", record.getBikeNo())
                    .append("contrail", list);
            mongoTemplate.insert(insertObj, "ride_contrail");
        } else {
            //存在时:
            Query query = new Query(Criteria.where("record_no").is(record.getRecordNo()));
            Update update = new Update().push("contrail", new BasicDBObject("loc", bikeLocation.getCoordinates()));
            mongoTemplate.updateFirst(query, update, "ride_contrail");
        }
    }

}

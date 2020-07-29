package com.bike.bike.service;

import com.bike.bike.entity.BikeLocation;
import com.bike.bike.entity.Point;
import com.bike.common.exception.MaMaBikeException;
import com.bike.record.entity.RideContrail;
import com.mongodb.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 单车定位服务类,使用MongoDB实现
 */
@Component//spring管理扫描到
@Slf4j
public class BikeGeoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 1、查找当前坐标点附近一定范围内坐标点,由近到远
     */
    public List<BikeLocation> geoNearSphere(String collection, String locationField, Point center, long minDistance,
                                            long maxDistance, DBObject query, DBObject fields, int limit) throws MaMaBikeException {
        try {
            if (query == null) {
                query = new BasicDBObject();
            }
            query.put(locationField,
                    new BasicDBObject("$nearSphere",
                            new BasicDBObject("$geometry",
                                    new BasicDBObject("type", "Point")
                                            .append("coordinates", new double[]{center.getLongitude(), center.getLatitude()}))
                                    .append("$minDistance", minDistance)
                                    .append("$maxDistance", maxDistance)
                    ));
            query.put("status", 1);
            List<DBObject> objList = mongoTemplate.getCollection(collection).find(query, fields).limit(limit).toArray();
            List<BikeLocation> result = new ArrayList<>();
            for (DBObject obj : objList) {
                BikeLocation location = new BikeLocation();
                location.setBikeNumber(((Integer) obj.get("bike_no")).longValue());
                location.setStatus((Integer) obj.get("status"));
                BasicDBList coordinates = (BasicDBList) ((BasicDBObject) obj.get("location")).get("coordinates");
                Double[] temp = new Double[2];
                coordinates.toArray(temp);
                location.setCoordinates(temp);
                result.add(location);
            }
            return result;
        } catch (Exception e) {
            log.error("fail to find around bike", e);
            throw new MaMaBikeException("查找附近单车失败");
        }
    }

    /**
     * 2、查找当前坐标点附近一定范围内坐标点,由近到远,并且计算距离
     */
    public List<BikeLocation> geoNear(String collection, DBObject query, Point point, int limit, long maxDistance) throws MaMaBikeException {
        try {
            if (query == null) {
                query = new BasicDBObject();
            }
            List<DBObject> pipeLine = new ArrayList<>();
            BasicDBObject aggregate = new BasicDBObject("$geoNear",
                    new BasicDBObject("near", new BasicDBObject("type", "Point")
                            .append("coordinates", new double[]{point.getLongitude(), point.getLatitude()}))
                            .append("distanceField", "distance")
                            .append("query", new BasicDBObject())//查询条件
                            .append("num", limit)//数据条数
                            .append("maxDistance", maxDistance)
                            .append("spherical", true)//是否使用计算
                            .append("query", new BasicDBObject("status", 1))
            );
            pipeLine.add(aggregate);
            Cursor cursor = mongoTemplate.getCollection(collection).aggregate(pipeLine, AggregationOptions.builder().build());
            List<BikeLocation> result = new ArrayList<>();
            while (cursor.hasNext()) {
                DBObject obj = cursor.next();
                BikeLocation location = new BikeLocation();
                location.setBikeNumber(((Integer) obj.get("bike_no")).longValue());
                BasicDBList coordinates = (BasicDBList) ((BasicDBObject) obj.get("location")).get("coordinates");
                Double[] temp = new Double[2];
                coordinates.toArray(temp);
                location.setCoordinates(temp);
                location.setDistance((Double) obj.get("distance"));
                result.add(location);
            }
            return result;
        } catch (Exception e) {
            log.error("fail to find around bike", e);
            throw new MaMaBikeException("查找附近单车失败");
        }
    }

    /**
     * 3、查询单车轨迹
     */
    public RideContrail rideContrail(String collection, String recordNo) throws MaMaBikeException {
        try {
            DBObject obj = mongoTemplate.getCollection(collection).findOne(new BasicDBObject("record_no", recordNo));
            RideContrail rideContrail = new RideContrail();
            rideContrail.setRideRecordNo((String) obj.get("record_no"));
            rideContrail.setBikeNo(((Integer) obj.get("bike_no")).longValue());
            BasicDBList locList = (BasicDBList) obj.get("contrail");
            List<Point> pointList = new ArrayList<>();
            for (Object object : locList) {
                BasicDBList locObj = (BasicDBList) ((BasicDBObject) object).get("loc");
                Double[] temp = new Double[2];
                locObj.toArray(temp);
                Point point = new Point(temp);
                pointList.add(point);
            }
            rideContrail.setContrail(pointList);
            return rideContrail;
        } catch (Exception e) {
            log.error("fail to query ride contrail", e);
            throw new MaMaBikeException("查询单车轨迹失败");
        }
    }

}

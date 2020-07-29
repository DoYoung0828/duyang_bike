package com.bike.bike.entity;

import lombok.Data;

@Data
public class Point {

    private double longitude;//经度
    private double latitude;//纬度

    public Point() {
    }

    public Point(Double[] loc){
        this.longitude = loc[0];
        this.latitude = loc[1];
    }

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

}

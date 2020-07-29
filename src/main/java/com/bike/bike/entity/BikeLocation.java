package com.bike.bike.entity;

import lombok.Data;

/**
 * Created by JackWangon[www.coder520.com] 2017/8/8.
 */
@Data
public class BikeLocation {

    private String id;

    private Long bikeNumber;

    private int status;

    private Double[] coordinates;

    private Double distance;

}

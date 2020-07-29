package com.bike.fee.entity;

import java.math.BigDecimal;

public class RideFee {

    private Long id;

    private Integer minUnit;

    private BigDecimal fee;

    private Byte bikeType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMinUnit() {
        return minUnit;
    }

    public void setMinUnit(Integer minUnit) {
        this.minUnit = minUnit;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Byte getBikeType() {
        return bikeType;
    }

    public void setBikeType(Byte bikeType) {
        this.bikeType = bikeType;
    }

}
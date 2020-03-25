package com.gujin.lottery.entity;

/**
 * Copyright © 2020年 lottery. All rights reserved.
 *
 * @author 古今
 * <p>
 * 概率实体
 * @date 2020/3/9
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2020/3/9   古今   v1.0.0       新增
 */
public class Rate {
    private String id;

    private double rate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}

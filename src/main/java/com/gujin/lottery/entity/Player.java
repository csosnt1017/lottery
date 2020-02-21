package com.gujin.lottery.entity;

public class Player {
    private String name;

    private Integer prizeSign = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrizeSign() {
        return prizeSign;
    }

    public void setPrizeSign(Integer prizeSign) {
        this.prizeSign = prizeSign;
    }
}

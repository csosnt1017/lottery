package com.gujin.lottery.exception;

public enum LotteryError {
    GAMING(301,"游戏已经开始，无法再加入！"),
    GAME_NOT_STARTED(302,"游戏未开始！");

    private int code;
    private String info;

    LotteryError(int code, String info) {
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}

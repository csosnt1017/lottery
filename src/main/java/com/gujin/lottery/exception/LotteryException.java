package com.gujin.lottery.exception;

/**
 * Copyright © 2019年 l7showmng. All rights reserved.
 *
 * @author 古今
 * <p>
 * 自定义异常类
 * @date 2019/03/25
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2019/03/25   古今   v1.0.0       新增
 */
public class LotteryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String msg;
    private int code;

    public LotteryException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public LotteryException(LotteryError lotteryError) {
        super(lotteryError.getInfo());
        this.code = lotteryError.getCode();
        this.msg = lotteryError.getInfo();
    }

    public LotteryException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public LotteryException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

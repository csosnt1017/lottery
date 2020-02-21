package com.gujin.lottery.api;

import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;

/**
 * @Author: yeoman
 * @Date: 2019/3/11 11:24
 */
public class JsonResult<T> {

    private int code = HttpStatus.OK.value();
    private String msg = "成功";
    private T data;

    public int getCode() {
        return code;
    }

    public JsonResult<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public JsonResult<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public JsonResult<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + (null != data ? data.toString() : null) +
                '}';
    }

    protected JsonResult getSuccessResult() {
        return new JsonResult();
    }

    @SuppressWarnings("unchecked")
    protected JsonResult<T> getSuccessDataResult(T data) {
        return new JsonResult<T>()
                .setData(data);
    }

    @SuppressWarnings("unchecked")
    protected JsonResult<ModelMap> getSuccessDataResult(String key, Object obj) {
        return new JsonResult<ModelMap>()
                .setData(new ModelMap(key, obj));
    }

    protected JsonResult getFailResult(int failCode, String msg) {
        return new JsonResult()
                .setCode(failCode)
                .setMsg(msg);
    }
}

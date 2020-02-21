package com.gujin.lottery.exception;

import com.gujin.lottery.api.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: yeoman
 * @Date: 2019/1/8 16:01
 */
@RestControllerAdvice
public class RequestErrorHandler extends JsonResult {
    protected static final Logger log = LoggerFactory.getLogger(RequestErrorHandler.class);

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(LotteryException.class)
    public JsonResult handleQSException(LotteryException e) {
        return getFailResult(e.getCode(), e.getMsg());
    }
}

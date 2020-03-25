package com.gujin.lottery.controller;

import com.gujin.lottery.exception.LotteryError;
import com.gujin.lottery.exception.LotteryException;
import com.gujin.lottery.socket.MySocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Copyright © 2020年 lottery. All rights reserved.
 *
 * @author 古今
 * <p>
 * api
 * @date 2020/3/14
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2020/3/14   古今   v1.0.0       新增
 */
@RestController
@RequestMapping("/socket")
public class GameController {
    /**
     * 第一个用户
     *
     * @return
     */
    @PostMapping("/join")
    public String join(@RequestParam(required = false) String name) {
        if (StringUtils.isEmpty(name)) {
            return LotteryError.NOT_NULL_NAME.getInfo();
        }
        if (MySocketServer.SESSIONS.get(name) != null || "admin".equals(name)) {
            return LotteryError.ALREADY_EXIST.getInfo();
        }
        if(MySocketServer.gameService.isGaming){
            return LotteryError.GAMING.getInfo();
        }
        return null;
    }

}

package com.gujin.lottery.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gujin.lottery.exception.LotteryError;
import com.gujin.lottery.exception.LotteryException;
import com.gujin.lottery.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright © 2020年 lottery. All rights reserved.
 *
 * @author 古今
 * <p>
 * xx
 * @date 2020/3/14
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2020/3/14   古今   v1.0.0       新增
 */
@Component
public class MySocketHander implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(MySocketHander.class);
    @Autowired
    GameService gameService;

    private final static Map<String, WebSocketSession> SESSIONS = Collections.synchronizedMap(new ConcurrentHashMap<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("链接成功......");
        String name = (String) session.getAttributes().get("name");
        if (SESSIONS.get(name) != null) {
            throw new LotteryException(LotteryError.ALREADY_EXIST);
        }
        SESSIONS.put(name, session);
        if (!"admin".equals(name)) {
            gameService.joinGame(session.getId(), name);
        }
        if (name != null) {
            JSONObject obj = new JSONObject();
            if ("admin".equals(name)) {
                WebSocketSession adminSession = SESSIONS.get("admin");
                obj.put("msg", "玩家" + name + "加入了游戏！");
                adminSession.sendMessage(new TextMessage(obj.toJSONString()));
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("处理要发送的消息");
        JSONObject msg = JSON.parseObject(message.getPayload().toString());
        JSONObject obj = new JSONObject();
        if (msg.getInteger("type") == 1) {
            //给所有人
            obj.put("msg", msg.getString("msg"));
            sendMessageToUsers(new TextMessage(obj.toJSONString()));
        } else {
            //给个人
            String to = msg.getString("to");
            obj.put("msg", msg.getString("msg"));
            sendMessageToUser(to, new TextMessage(obj.toJSONString()));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        log.info("链接出错，关闭链接......");
    }

    @Override
    @OnClose
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("链接关闭......" + closeStatus.toString());
        gameService.finishGame();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 给某个用户发送消息
     *
     * @param userName
     * @param message
     */
    public void sendMessageToUser(String userName, TextMessage message) {
        log.info("发消息给某个用户");
        WebSocketSession webSocketSession = SESSIONS.get(userName);
        if (webSocketSession.isOpen()) {
            try {
                webSocketSession.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给所有在线用户发送消息
     *
     * @param message
     */
    public void sendMessageToUsers(TextMessage message) {
        log.info("发送消息给所用用户");
        for (Map.Entry<String, WebSocketSession> session : SESSIONS.entrySet()) {
            try {
                if (session.getValue().isOpen()) {
                    session.getValue().sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

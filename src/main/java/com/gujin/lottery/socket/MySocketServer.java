package com.gujin.lottery.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gujin.lottery.entity.Player;
import com.gujin.lottery.exception.LotteryError;
import com.gujin.lottery.exception.LotteryException;
import com.gujin.lottery.service.GameService;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright © 2020年 lottery. All rights reserved.
 *
 * @author 古今
 * <p>
 * xx
 * @date 2020/3/15
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2020/3/15   古今   v1.0.0       新增
 */
@ServerEndpoint("/socket")
@Component
public class MySocketServer {
    private static final Logger log = LoggerFactory.getLogger(MySocketServer.class);
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static volatile int onlineCount = 0;

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    public final static Map<String, MySocketServer> SESSIONS = Collections.synchronizedMap(new ConcurrentHashMap<>());
    public static final GameService gameService = new GameService();

    private String name;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        String url = session.getRequestURI().toString();
        int i = url.lastIndexOf("=");
        String name = url.substring(i + 1);
        this.name = name;
        log.info("链接成功......");
        SESSIONS.put(name, this);
        if (!"admin".equals(name)) {
            gameService.joinGame(session.getId(), name);
            MySocketServer admin = SESSIONS.get("admin");
            if (admin != null) {
                JSONObject obj = new JSONObject();
                obj.put("msg", "玩家" + name + "加入了游戏！");
                admin.sendMessage(obj.toJSONString());
            }

        }
        System.out.println("用户连接:" + name + ",当前在线人数为:" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        if ("admin".equals(this.name)) {
            gameService.finishGame();
            SESSIONS.clear();
        } else {
            MySocketServer admin = SESSIONS.get("admin");
            if (admin != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msg", "玩家" + name + "退出了游戏！");
                admin.sendMessage(jsonObject.toJSONString());
            }
            System.out.println("玩家" + name + "退出了游戏！");
            SESSIONS.remove(this.name);
        }
        session.close();
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("用户消息:" + ",报文:" + message);
        //可以群发消息
        //消息保存到数据库、redis
        if (!StringUtils.isEmpty(message)) {
            try {
                //解析发送的报文
                JSONObject jsonObject = JSON.parseObject(message);
                String type = jsonObject.getString("type");
                if ("1".equals(type)) {
                    gameService.openExtractingProps();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("sign", 1);
                    SESSIONS.forEach((k, v) -> {
                        try {
                            if (!"admin".equals(k)) {
                                v.sendMessage(jsonObject1.toJSONString());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else if ("2".equals(type)) {
                    JSONObject jsonObject1 = new JSONObject();
                    Player player = gameService.extractingProps(session.getId());
                    if (!gameService.isGaming) {
                        SESSIONS.get(player.getName()).sendMessage(LotteryError.GAME_NOT_STARTED.getInfo());
                        return;
                    }
                    jsonObject1.put("player", player);
                    if (SESSIONS.get("admin") != null) {
                        if (player.getPrizeSign() != 0) {
                            SESSIONS.get("admin").sendMessage(jsonObject1.toJSONString());
                        }
                    }
                    SESSIONS.get(player.getName()).sendMessage(jsonObject1.toJSONString());
                } else if ("3".equals(type)) {
                    JSONObject jsonObject1 = new JSONObject();
                    Player player = gameService.startLotterring();
                    jsonObject1.put("lotteriedPlayer", player);
                    SESSIONS.forEach((k, v) -> {
                        try {
                            v.sendMessage(jsonObject1.toJSONString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else if ("4".equals(type)) {
                    String name = jsonObject.getString("name");
                    gameService.clearPrize(gameService.getId(name));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, @PathParam("userId") String userId) throws IOException {
        SESSIONS.get(userId).sendMessage(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MySocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MySocketServer.onlineCount--;
    }
}

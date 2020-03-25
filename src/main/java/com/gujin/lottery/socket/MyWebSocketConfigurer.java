package com.gujin.lottery.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Copyright © 2020年 lottery. All rights reserved.
 *
 * @author 古今
 * <p>
 * socket配置
 * @date 2020/3/14
 * <p>
 * Modification History:
 * Date     Author    Version      Description
 * ---------------------------------------------------------*
 * 2020/3/14   古今   v1.0.0       新增
 */
@Configuration
@EnableWebSocket
public class MyWebSocketConfigurer implements WebSocketConfigurer {
    @Autowired
    MyHandshakeInterceptor handshakeInterceptor;
    @Autowired
    MySocketHander socketHander;


    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
    /**
     * 实现 WebSocketConfigurer 接口
     * 重写 registerWebSocketHandlers 方法，这是一个核心实现方法，配置 websocket 入口，允许访问的域、注册 Handler、SockJs 支持和拦截器。
     * <p>
     * registry.addHandler()注册和路由的功能
     * 当客户端发起 websocket 连接，把 /path 交给对应的 handler 处理，而不实现具体的业务逻辑，可以理解为收集和任务分发中心。
     * <p>
     * <p>
     * addInterceptors() 顾名思义就是为 handler 添加拦截器
     * 可以在调用 handler 前后加入我们自己的逻辑代码。
     * <p>
     * <p>
     * setAllowedOrigins(String[] domains),允许指定的域名或 IP (含端口号)建立长连接
     * 如果只允许自家域名访问，这里轻松设置。如果不限时使用 * 号，如果指定了域名，则必须要以 http 或 https 开头。
     *
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //部分 支持websocket 的访问链接,允许跨域
        registry.addHandler(socketHander, "/socket").addInterceptors(handshakeInterceptor).setAllowedOrigins("*");
    }
}

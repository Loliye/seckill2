package com.mikufans.seckill.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket/{userId}")
@Component
@Slf4j
public class WebSocketServer
{
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onLineCount = 0;

    //concurrent包的线程安全set，用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<WebSocketServer> webSocketServers = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接受userId
    private String userId = "";

    /**
     * 群发自定义消息
     * @param message
     * @param userId
     */
    public static void sendInfo(String message, @PathParam("userId") String userId)
    {
        log.info("推送消息到窗口" + userId + "，推送内容:" + message);
        for (WebSocketServer item : webSocketServers)
        {
            try
            {
                if (userId == null)
                    item.sendMessage(message);
                else if (item.userId.equals(userId))
                    item.sendMessage(message);
            } catch (IOException e)
            {
                log.error("websocket群发出错");
                e.printStackTrace();
            }
        }
    }

    public static synchronized int getOnlineCount()
    {
        return onLineCount;
    }

    public static synchronized void addOnlineCount()
    {
        WebSocketServer.onLineCount++;
    }

    public static synchronized void subOnlineCount()
    {
        WebSocketServer.onLineCount--;
    }

    /**
     * 连接建立成功电泳的方法
     *
     * @param session
     * @param userId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId)
    {
        this.session = session;
        webSocketServers.add(this);
        addOnlineCount();
        log.info("有新窗口开始监听:" + userId + ",当前在线人数为" + getOnlineCount());
        this.userId = userId;
        try
        {
            sendMessage("连接成功");
        } catch (IOException e)
        {
            log.error("websocket io 异常");
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose()
    {
        webSocketServers.remove(this);
        subOnlineCount();
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message, Session session)
    {
        log.info("收到来自窗口" + userId + "的信息:" + message);
        //群发消息
        for (WebSocketServer item : webSocketServers)
        {
            try
            {
                item.sendMessage(message);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error)
    {
        log.error("websocket发生错误");
        error.printStackTrace();
    }

    /**
     * 服务器自动推送消息
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException
    {
        this.session.getBasicRemote().sendText(message);
    }

}

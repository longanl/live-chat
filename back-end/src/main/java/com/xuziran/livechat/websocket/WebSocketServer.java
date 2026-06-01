package com.xuziran.livechat.websocket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuziran.livechat.constant.MessageConstant;
import com.xuziran.livechat.json.JacksonObjectMapper;
import com.xuziran.livechat.mapper.MessagesMapper;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.pojo.DTO.MessageDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.pojo.VO.MessageVO;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebSocket服务
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {
    // 静态变量保存 MessagesMapper 实例
    private static MessagesMapper messagesMapper;
    private static UserMapper userMapper;
    private static JacksonObjectMapper objectMapper =new JacksonObjectMapper();

    // 由 Spring 注入 MessagesMapper 到静态变量
    @Autowired
    public void setMessagesMapper(MessagesMapper messagesMapper) {
        WebSocketServer.messagesMapper = messagesMapper;
    }
    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    //存放会话对象
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        //添加会话对象
        sessionMap.put(sid, session);
        //转发在线用户信息
        List<User> onlineUserList=userMapper.queryOnlineUser();
        Map<String,Object> map=new HashMap();
        map.put("type","onlineUsers");
        map.put("content",onlineUserList);
        String json=packJson(map);
        sendToAllClient(json);
        //转发群人数
        Long count = userMapper.count();
        log.info("转发群人数为: {}", count);
        map.put("type","onlineCount");
        map.put("content",count);
        json=packJson(map);
        sendToAllClient(json);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
        Map<String,Object> messageData = parseMessage(message);
        if("group".equals(messageData.get("type"))){
            MessageDTO messageDTO = objectMapper.convertValue(messageData.get("content"), MessageDTO.class);
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(messageDTO, messageVO);
            User user = messagesMapper.queryUserById(messageVO.getSenderId());
            messageVO.setAvatar(user.getAvatar());
            messageVO.setNickname(user.getNickname());
            messageVO.setMessageType(MessageConstant.MESSAGE_TYPE_TEXT);
            messageVO.setSendTime(LocalDateTime.now());
            messageVO.setReadStatus(0);
            Map map = new HashMap();
            map.put("type","group");
            map.put("content",messageVO);
            String json=packJson(map);
            sendToAllClient(json);
            log.info("转发信息为: {}", json);
            messagesMapper.insertMessage(messageVO);
        } else if ("p2pchat".equals(messageData.get("type"))) {
            MessageDTO messageDTO = objectMapper.convertValue(messageData.get("content"), MessageDTO.class);
            MessageVO messageVO = new MessageVO();
            BeanUtils.copyProperties(messageDTO, messageVO);
            User user = messagesMapper.queryUserById(messageVO.getSenderId());
            messageVO.setAvatar(user.getAvatar());
            messageVO.setNickname(user.getNickname());
            messageVO.setMessageType(MessageConstant.MESSAGE_TYPE_TEXT);
            messageVO.setSendTime(LocalDateTime.now());
            messageVO.setReadStatus(0);
            Map map = new HashMap();
            map.put("type","p2pchat");
            map.put("content",messageVO);
            String json=packJson(map);
            sendToAllClient(json);
            log.info("转发信息为: {}", json);
            messagesMapper.insertMessage(messageVO);
        }
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        sessionMap.remove(sid);
    }
    public String packJson(Map<String,Object> map){
        try {
            return objectMapper.writeValueAsString(map);

        } catch (JsonProcessingException e) {
            log.info("转换失败，原因：{}，原始数据：{}", e.getMessage(), map, e);
            throw new RuntimeException(e);
        }
    }

    public Map<String,Object> parseMessage(String message){
        try {
            return objectMapper.readValue(message, Map.class);//json转换成Map
        } catch (JsonProcessingException e) {
            log.info("解析失败，原因：{}，原始消息：{}", e.getMessage(), message, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 群发
     *
     * @param message
     */
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                log.info("转发信息给所有人: {}", message);
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.info("转发失败");
                e.printStackTrace();
                sessionMap.values().remove(session);//移除掉已经断开的session
            }
        }
    }

    /**
     * 私聊
     *
     * @param message
     */
    public void sendToOneClient(String message, String sid) {

    Session session = sessionMap.get(sid);
    try{
        session.getBasicRemote().sendText(message);
    } catch(
    Exception e)
    {
        log.info("转发失败");
        e.printStackTrace();
        sessionMap.values().remove(session);//移除掉已经断开的session
    }
        log.info("转发信息为: {}",message);
    }
}

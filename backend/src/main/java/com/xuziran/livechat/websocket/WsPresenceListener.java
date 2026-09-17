package com.xuziran.livechat.websocket;

import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.OnlineUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线状态管理：
 * - 连接建立：更新 users.status=1，并向所有在线客户端广播在线列表/人数；
 * - 连接断开：最后一个会话断开时把用户置为离线，再广播在线列表/人数。
 *
 * 新连接客户端的初始快照不在此处推送（存在 STOMP 订阅竞态），
 * 由客户端连接后主动发送 /app/presence.snapshot 请求，服务端再定向回复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsPresenceListener {

    private static final String PRESENCE_USERS_DEST = "/topic/online/users";
    private static final String PRESENCE_COUNT_DEST = "/topic/online/count";

    private final WsSessionRegistry wsSessionRegistry;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        Long userId = wsSessionRegistry.getUserId(sessionId);
        if (userId == null) {
            return;
        }
        setStatus(userId, 1);
        broadcastPresence();
    }

    @EventListener
    public void onDisconnected(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long userId = wsSessionRegistry.unregister(sessionId);
        if (userId == null) {
            return;
        }
        if (!wsSessionRegistry.isOnline(userId)) {
            setStatus(userId, 0);
        }
        broadcastPresence();
    }

    private void setStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        userMapper.update(user);
    }

    private void broadcastPresence() {
        List<User> onlineUsers = userMapper.queryOnlineUser();
        List<OnlineUserDTO> dtos = onlineUsers.stream()
                .map(u -> OnlineUserDTO.builder().id(u.getId()).username(u.getUsername())
                        .nickname(u.getNickname()).avatar(u.getAvatar()).build())
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend(PRESENCE_USERS_DEST, dtos);
        messagingTemplate.convertAndSend(PRESENCE_COUNT_DEST, onlineUsers.size());
    }
}
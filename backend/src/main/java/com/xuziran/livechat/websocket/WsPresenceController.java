package com.xuziran.livechat.websocket;

import com.xuziran.livechat.common.constant.Constant;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.OnlineUserDTO;
import com.xuziran.livechat.model.vo.PresenceSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线状态查询：客户端连接完成后订阅 /user/queue/presence，再发送 /app/presence.snapshot，
 * 服务端以「请求-响应」方式定点回复初始在线快照，避免订阅完成后广播时序丢失。
 */
@Controller
@RequiredArgsConstructor
public class WsPresenceController {



    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/presence.snapshot")
    public void snapshot(Principal principal) {
        List<User> onlineUsers = userMapper.queryOnlineUser();
        List<OnlineUserDTO> dtos = onlineUsers.stream()
                .map(u -> OnlineUserDTO.builder().id(u.getId()).username(u.getUsername())
                        .nickname(u.getNickname()).avatar(u.getAvatar()).build())
                .collect(Collectors.toList());
        PresenceSnapshot snapshot = PresenceSnapshot.builder()
                .users(dtos)
                .count(dtos.size())
                .build();
        messagingTemplate.convertAndSendToUser(safeName(principal), Constant.PRESENCE_SNAPSHOT_QUEUE, snapshot);
    }

    /** Principal 名称即 JWT 中的 userId；兼容非法值兜底。 */
    private String safeName(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("未认证的 WebSocket 连接");
        }
        return principal.getName();
    }
}
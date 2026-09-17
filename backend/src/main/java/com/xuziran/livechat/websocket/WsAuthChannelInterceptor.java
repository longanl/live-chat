package com.xuziran.livechat.websocket;

import com.xuziran.livechat.common.properties.JwtProperties;
import com.xuziran.livechat.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * STOMP 连接鉴权：拦截 CONNECT 帧，校验 JWT，并把用户ID设为当前会话的 Principal。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsAuthChannelInterceptor implements ChannelInterceptor {

    private static final String TOKEN_HEADER = "token";

    private final JwtProperties jwtProperties;
    private final WsSessionRegistry wsSessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }
        String token = accessor.getFirstNativeHeader(TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            log.warn("STOMP 连接缺少 token，拒绝连接");
            throw new MessagingException("缺少 token");
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long userId = ((Number) claims.get("id")).longValue();
            accessor.setUser(userId::toString);
            wsSessionRegistry.register(userId, accessor.getSessionId());
            log.info("STOMP 连接鉴权通过：userId={}, sessionId={}", userId, accessor.getSessionId());
        } catch (Exception e) {
            log.warn("STOMP 连接鉴权失败：{}", e.getMessage());
            throw new MessagingException("token 无效或已过期");
        }
        return message;
    }
}
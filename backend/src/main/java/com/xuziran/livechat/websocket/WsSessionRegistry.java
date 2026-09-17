package com.xuziran.livechat.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话注册表：sessionId -> userId，以及 userId -> 活跃会话集合。
 *
 * 用于：
 * 1) 断线事件能通过 sessionId 回查到所属用户；
 * 2) 多标签页同时在线时，最后一个会话断开才把用户置为离线。
 */
@Component
public class WsSessionRegistry {

    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void register(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return;
        }
        sessionUsers.put(sessionId, userId);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public Long getUserId(String sessionId) {
        return sessionUsers.get(sessionId);
    }

    /** 注销一条会话，返回其所属用户（会话不存在时返回 null）。 */
    public Long unregister(String sessionId) {
        Long userId = sessionUsers.remove(sessionId);
        if (userId == null) {
            return null;
        }
        Set<String> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        return userId;
    }

    /** 用户是否还有活跃会话。 */
    public boolean isOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /** 注销该用户所有会话，返回是否仍有其他活跃会话（true=还有，false=已全部注销） */
    public boolean unregisterAll(Long userId) {
        Set<String> sessions = userSessions.remove(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        for (String sessionId : sessions) {
            sessionUsers.remove(sessionId);
        }
        return true;
    }
}
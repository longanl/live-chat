package com.xuziran.livechat.service;

import com.xuziran.livechat.model.dto.MessageDTO;
import com.xuziran.livechat.model.vo.MessageVO;
import com.xuziran.livechat.model.vo.UnreadStat;

import java.util.List;

public interface MessagesService {
    /** 会话历史游标分页（需校验当前用户为该会话成员） */
    List<MessageVO> queryByConversation(Long conversationId, Long beforeId, Integer limit, Long userId);

    /**
     * 根据 WebSocket 消息类型与收发双方解析出会话ID。
     * group -> 内置群会话；p2pchat -> 查找或惰性创建私聊会话（含成员）。
     */
    Long resolveConversation(String type, Long senderId, Long receiverId);

    /**
     * 解析群聊会话：指定 conversationId 时校验该用户是否为群成员（防越权发言）；
     * conversationId 为空时回落到内置群会话，兼容旧客户端。
     */
    Long resolveGroupConversation(Long conversationId, Long senderId);

    /** 创建 / 获取与对方的私聊会话，返回会话ID（仅限好友，不存在则创建并补齐成员） */
    Long createPrivateConversation(Long userId, Long targetUserId);

    /**
     * 持久化消息并构造返回 VO（WebSocket / HTTP 共用），同时校验发送者为会话成员。
     * 文件消息判定：messageType=2 且 fileUrl 非空，否则按文本处理。
     */
    MessageVO saveMessage(Long senderId, Long conversationId, MessageDTO dto);

    /** 推送消息：群聊广播到 /topic/conv/{id}，私聊推送到全部成员的 /user/{id}/queue/messages */
    void broadcastMessage(MessageVO vo);

    /** 推进已读游标（需校验当前用户为该会话成员） */
    void markRead(Long conversationId, Long userId);

    /** 当前用户的未读统计 */
    List<UnreadStat> unread(Long userId);
}
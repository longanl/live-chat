package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.common.constant.Constant;
import com.xuziran.livechat.common.exception.BusinessException;
import com.xuziran.livechat.mapper.FriendsMapper;
import com.xuziran.livechat.mapper.MessagesMapper;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.model.dto.MessageDTO;
import com.xuziran.livechat.model.entity.ChatMessage;
import com.xuziran.livechat.model.entity.Conversation;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.MessageVO;
import com.xuziran.livechat.model.vo.UnreadStat;
import com.xuziran.livechat.service.MessagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MessagesServiceImpl implements MessagesService {

    /** 内置群会话默认 ID（init.sql 固定为 1） */
    private static final Long GROUP_CONVERSATION_ID = 1L;
    private static final Integer CONVERSATION_TYPE_GROUP = 2;
    private static final Integer CONVERSATION_TYPE_P2P = 1;

    @Autowired
    private MessagesMapper messagesMapper;

    @Autowired
    private FriendsMapper friendsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public List<MessageVO> queryByConversation(Long conversationId, Long beforeId, Integer limit, Long userId) {
        requireMember(conversationId, userId);
        int size = (limit == null || limit > 100 || limit < 1) ? 30 : limit;
        List<MessageVO> list = messagesMapper.selectHistoryPage(conversationId, beforeId, size);
        // 后端取的是最新的 size 条（id 降序），翻转回时间正序，前端直接渲染
        Collections.reverse(list);
        return list;
    }

    @Override
    @Transactional
    public Long resolveConversation(String type, Long senderId, Long receiverId) {
        if ("group".equals(type)) {
            Conversation group = messagesMapper.selectConversationById(GROUP_CONVERSATION_ID);
            if (group == null) {
                Conversation c = Conversation.builder()
                        .type(CONVERSATION_TYPE_GROUP)
                        .name("技术讨论群")
                        .build();
                messagesMapper.insertConversation(c);
            }
            messagesMapper.insertMember(GROUP_CONVERSATION_ID, senderId);
            return GROUP_CONVERSATION_ID;
        }
        // 私聊仅限好友：非好友既不能新建会话，也不能在历史遗留会话中发送
        long lower = Math.min(senderId, receiverId);
        long upper = Math.max(senderId, receiverId);
        if (friendsMapper.countRelation(lower, upper) == 0) {
            throw new BusinessException("对方不是你的好友，无法私聊");
        }
        // 私聊：按用户对查找，不存在则创建并补齐两方成员
        Long convId = messagesMapper.selectP2PConversationId(senderId, receiverId);
        if (convId == null) {
            Conversation c = Conversation.builder()
                    .type(CONVERSATION_TYPE_P2P)
                    .build();
            messagesMapper.insertConversation(c);
            convId = c.getId();
            messagesMapper.insertMember(convId, senderId);
            messagesMapper.insertMember(convId, receiverId);
        }
        return convId;
    }

    @Override
    public Long resolveGroupConversation(Long conversationId, Long senderId) {
        // 未指定会话：回落到内置群（兼容旧客户端与老前端）
        if (conversationId == null) {
            return resolveConversation(Constant.GROUP, senderId, null);
        }
        Conversation conversation = messagesMapper.selectConversationById(conversationId);
        if (conversation == null || !CONVERSATION_TYPE_GROUP.equals(conversation.getType())) {
            throw new BusinessException("群聊不存在");
        }
        // 防越权：必须是该群成员才能发言
        requireMember(conversationId, senderId);
        return conversationId;
    }

    @Override
    @Transactional
    public Long createPrivateConversation(Long userId, Long targetUserId) {
        if (targetUserId == null) {
            throw new BusinessException("缺少对方用户ID");
        }
        // 复用私聊会话解析：仅限好友，不存在则创建并补齐双方成员
        return resolveConversation(Constant.P2P, userId, targetUserId);
    }

    @Override
    @Transactional
    public MessageVO saveMessage(Long senderId, Long conversationId, MessageDTO dto) {
        if (conversationId == null || dto == null) {
            throw new BusinessException("参数不完整");
        }
        requireMember(conversationId, senderId);
        User user = userMapper.getById(senderId);
        boolean isFile = Constant.MESSAGE_TYPE_FILE.equals(dto.getMessageType())
                && dto.getFileUrl() != null && !dto.getFileUrl().isBlank();
        ChatMessage chatMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .messageType(isFile ? Constant.MESSAGE_TYPE_FILE : Constant.MESSAGE_TYPE_TEXT)
                .content(dto.getContent())
                .fileUrl(dto.getFileUrl())
                .fileName(dto.getFileName())
                .fileSize(dto.getFileSize())
                .fileType(dto.getFileType())
                .clientMsgId(dto.getClientMsgId())
                .sendTime(LocalDateTime.now())
                .build();
        try {
            messagesMapper.insertMessage(chatMessage);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该消息已发送过（clientMsgId 重复）");
        }

        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(chatMessage, vo);
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }

    @Override
    public void broadcastMessage(MessageVO vo) {
        if (vo.getConversationId() == null) {
            return;
        }
        Conversation conversation = messagesMapper.selectConversationById(vo.getConversationId());
        if (conversation == null) {
            return;
        }
        if (CONVERSATION_TYPE_GROUP.equals(conversation.getType())) {
            // 群聊：实时在线订阅方广播
            messagingTemplate.convertAndSend(Constant.TOPIC_CONVERSATION_PREFIX + vo.getConversationId(), vo);
        } else {
            // 私聊：发给双方成员（含发送者自身回显），与 WS 通道行为一致
            List<User> members = messagesMapper.selectConversationMembers(vo.getConversationId());
            for (User member : members) {
                if (member.getId() == null) {
                    continue;
                }
                messagingTemplate.convertAndSendToUser(member.getId().toString(), Constant.MESSAGES_QUEUE, vo);
            }
        }
    }

    @Override
    @Transactional
    public void recall(Long messageId, Long userId) {
        int updated = messagesMapper.updateRecalled(messageId, userId);
        if (updated == 0) {
            throw new BusinessException("无法撤回该消息（仅发送者可撤回发送后2分钟内的消息）");
        }
    }

    @Override
    public void markRead(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            return;
        }
        requireMember(conversationId, userId);
        Long maxId = messagesMapper.selectMaxMsgId(conversationId);
        if (maxId == null) {
            return;
        }
        messagesMapper.markRead(conversationId, userId, maxId);
    }

    /** 会话成员校验：非成员直接拒绝，杜绝"猜 conversationId 越权读消息"与"标记已读即可自加入会话" */
    private void requireMember(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            throw new BusinessException("无权访问该会话");
        }
        Long cnt = messagesMapper.selectConversationMember(conversationId, userId);
        if (cnt == null || cnt == 0) {
            throw new BusinessException("无权访问该会话");
        }
    }

    @Override
    public List<UnreadStat> unread(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return messagesMapper.selectUnread(userId);
    }
}
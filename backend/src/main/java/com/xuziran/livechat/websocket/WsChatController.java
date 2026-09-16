package com.xuziran.livechat.websocket;

import com.xuziran.livechat.common.constant.Constant;
import com.xuziran.livechat.common.exception.BusinessException;
import com.xuziran.livechat.mapper.MessagesMapper;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.model.dto.MessageDTO;
import com.xuziran.livechat.model.entity.ChatMessage;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.MessageVO;
import com.xuziran.livechat.service.MessagesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * STOMP 消息入口：
 * - /app/chat.group：群聊，广播到 /topic/conv/{conversationId}；
 * - /app/chat.p2p：私聊，定向推送到收发双方的 /user/{id}/queue/messages。
 *
 * 发送者身份一律取 JWT 鉴权后的 Principal，不信任客户端传来的 senderId。
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WsChatController {
    private final MessagesService messagesService;
    private final MessagesMapper messagesMapper;
    private final UserMapper userMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.group")
    public void group(@Payload MessageDTO dto, Principal principal) {
        Long senderId = currentUserId(principal);
        // 指定 conversationId 时校验成员资格；未指定则回落到内置群会话
        Long conversationId = messagesService.resolveGroupConversation(dto.getConversationId(), senderId);
        MessageVO vo = saveAndBuild(dto, senderId, conversationId);
        messagingTemplate.convertAndSend(conversationTopic(conversationId), vo);
        log.info("群消息已广播：conversationId={}, senderId={}", conversationId, senderId);
    }

    @MessageMapping("/chat.p2p")
    public void p2p(@Payload MessageDTO dto, Principal principal) {
        Long senderId = currentUserId(principal);
        Long receiverId = dto.getReceiverId();
        if (receiverId == null) {
            log.warn("私聊消息缺少 receiverId，已忽略：{}", dto);
            return;
        }
        Long conversationId = messagesService.resolveConversation(Constant.P2P, senderId, receiverId);
        MessageVO vo = saveAndBuild(dto, senderId, conversationId);
        messagingTemplate.convertAndSendToUser(senderId.toString(), Constant.MESSAGES_QUEUE, vo);
        messagingTemplate.convertAndSendToUser(receiverId.toString(), Constant.MESSAGES_QUEUE, vo);
        log.info("私聊消息已推送：conversationId={}, from={}, to={}", conversationId, senderId, receiverId);
    }

    /** 业务异常（如非好友私聊、非群成员）定向推送给发送者，避免 ERROR 帧导致客户端断连。 */
    @MessageExceptionHandler(BusinessException.class)
    public void onBusinessException(BusinessException e, Principal principal) {
        log.warn("STOMP 业务异常：{}", e.getMessage());
        if (principal == null) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                principal.getName(), Constant.ERROR_QUEUE, Map.of("message", e.getMessage()));
    }

    private MessageVO saveAndBuild(MessageDTO dto, Long senderId, Long conversationId) {
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
                .sendTime(LocalDateTime.now())
                .build();
        messagesMapper.insertMessage(chatMessage);

        MessageVO messageVO = new MessageVO();
        BeanUtils.copyProperties(chatMessage, messageVO);
        if (user != null) {
            messageVO.setNickname(user.getNickname());
            messageVO.setAvatar(user.getAvatar());
        }
        return messageVO;
    }

    private Long currentUserId(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("未认证的 WebSocket 连接");
        }
        return Long.valueOf(principal.getName());
    }

    private String conversationTopic(Long conversationId) {
        return "/topic/conv/" + conversationId;
    }
}
package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.common.exception.BusinessException;
import com.xuziran.livechat.mapper.MessagesMapper;
import com.xuziran.livechat.model.dto.CreateGroupDTO;
import com.xuziran.livechat.model.dto.MemberIdsDTO;
import com.xuziran.livechat.model.entity.Conversation;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.ConversationVO;
import com.xuziran.livechat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final Integer TYPE_GROUP = 2;

    private final MessagesMapper messagesMapper;

    @Override
    public List<ConversationVO> listMine(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return messagesMapper.selectMyConversations(userId);
    }

    @Override
    public ConversationVO detail(Long conversationId, Long userId) {
        requireMember(conversationId, userId);
        return listMine(userId).stream()
                .filter(c -> conversationId.equals(c.getConversationId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("会话不存在"));
    }

    @Override
    public List<User> members(Long conversationId, Long userId) {
        requireMember(conversationId, userId);
        return messagesMapper.selectConversationMembers(conversationId);
    }

    @Override
    @Transactional
    public Long createGroup(Long ownerId, CreateGroupDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException("群名称不能为空");
        }
        String name = dto.getName().trim();
        if (name.length() > 100) {
            throw new BusinessException("群名称过长");
        }
        Conversation conversation = Conversation.builder()
                .type(TYPE_GROUP)
                .name(name)
                .ownerId(ownerId)
                .build();
        messagesMapper.insertConversation(conversation);
        Long conversationId = conversation.getId();
        messagesMapper.insertMember(conversationId, ownerId);
        List<Long> memberIds = distinctIds(dto.getMemberIds(), ownerId);
        if (!memberIds.isEmpty()) {
            messagesMapper.insertMembers(conversationId, memberIds);
        }
        log.info("创建群聊 conversationId={} owner={} members={}", conversationId, ownerId, memberIds.size());
        return conversationId;
    }

    @Override
    @Transactional
    public void addMembers(Long conversationId, Long operatorId, MemberIdsDTO dto) {
        requireMember(conversationId, operatorId);
        requireGroup(conversationId);
        List<Long> userIds = distinctIds(dto == null ? null : dto.getUserIds(), null);
        if (userIds.isEmpty()) {
            throw new BusinessException("请选择要添加的成员");
        }
        messagesMapper.insertMembers(conversationId, userIds);
        log.info("邀请入群 conversationId={} operator={} members={}", conversationId, operatorId, userIds.size());
    }

    @Override
    @Transactional
    public void quit(Long conversationId, Long userId) {
        requireMember(conversationId, userId);
        Conversation conversation = requireGroup(conversationId);
        if (conversation.getOwnerId() != null && conversation.getOwnerId().equals(userId)) {
            throw new BusinessException("群主不能退出群聊");
        }
        messagesMapper.deleteMember(conversationId, userId);
        log.info("退出群聊 conversationId={} userId={}", conversationId, userId);
    }

    @Override
    @Transactional
    public void kickMember(Long conversationId, Long operatorId, Long targetUserId) {
        requireMember(conversationId, operatorId);
        Conversation conversation = requireGroup(conversationId);
        if (conversation.getOwnerId() == null || !conversation.getOwnerId().equals(operatorId)) {
            throw new BusinessException("仅群主可以踢人");
        }
        if (targetUserId == null) {
            throw new BusinessException("参数错误");
        }
        if (operatorId.equals(targetUserId)) {
            throw new BusinessException("不能踢自己");
        }
        Long cnt = messagesMapper.selectConversationMember(conversationId, targetUserId);
        if (cnt == null || cnt == 0) {
            throw new BusinessException("该用户不是群成员");
        }
        messagesMapper.deleteMember(conversationId, targetUserId);
        log.info("踢出群成员 conversationId={} operator={} target={}", conversationId, operatorId, targetUserId);
    }

    /** 去重并剔除 null / 自身，避免批量 insert 出现重复值与脏数据 */
    private List<Long> distinctIds(List<Long> ids, Long excludeId) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(excludeId))
                .distinct()
                .toList();
    }

    /** 仅群聊支持的操作前置校验，返回会话本体 */
    private Conversation requireGroup(Long conversationId) {
        Conversation conversation = messagesMapper.selectConversationById(conversationId);
        if (conversation == null || !TYPE_GROUP.equals(conversation.getType())) {
            throw new BusinessException("群聊不存在");
        }
        return conversation;
    }

    /** 会话成员校验：非成员直接拒绝，杜绝越权读取会话与成员列表 */
    private void requireMember(Long conversationId, Long userId) {
        if (conversationId == null || userId == null) {
            throw new BusinessException("无权访问该会话");
        }
        Long cnt = messagesMapper.selectConversationMember(conversationId, userId);
        if (cnt == null || cnt == 0) {
            throw new BusinessException("无权访问该会话");
        }
    }
}
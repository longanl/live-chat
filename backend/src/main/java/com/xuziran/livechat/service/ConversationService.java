package com.xuziran.livechat.service;

import com.xuziran.livechat.model.dto.CreateGroupDTO;
import com.xuziran.livechat.model.dto.MemberIdsDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.ConversationVO;

import java.util.List;

/**
 * 会话 / 群组服务。所有方法都以「当前登录用户是该会话成员」为前提做校验，
 * 与 problems/19 的口径一致：不信任前端传入的身份与会话归属。
 */
public interface ConversationService {

    /** 我的会话列表：群聊 + 已建立的私聊 */
    List<ConversationVO> listMine(Long userId);

    /** 会话详情（需为会话成员） */
    ConversationVO detail(Long conversationId, Long userId);

    /** 会话成员列表（需为会话成员） */
    List<User> members(Long conversationId, Long userId);

    /** 创建群聊：创建者自动成为群主与成员，返回新会话ID */
    Long createGroup(Long ownerId, CreateGroupDTO dto);

    /** 邀请成员加入群聊（需为群成员） */
    void addMembers(Long conversationId, Long operatorId, MemberIdsDTO dto);

    /** 退出群聊（群主不允许退出，避免产生无主群） */
    void quit(Long conversationId, Long userId);

    /** 踢出成员（仅群主可操作，不能踢自己，目标必须为群成员） */
    void kickMember(Long conversationId, Long operatorId, Long targetUserId);
}
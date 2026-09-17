package com.xuziran.livechat.controller;

import com.xuziran.livechat.common.context.BaseContext;
import com.xuziran.livechat.model.dto.CreateGroupDTO;
import com.xuziran.livechat.model.dto.MemberIdsDTO;
import com.xuziran.livechat.model.dto.PrivateChatDTO;
import com.xuziran.livechat.model.dto.UpdateGroupDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.ConversationVO;
import com.xuziran.livechat.common.result.Result;
import com.xuziran.livechat.service.ConversationService;
import com.xuziran.livechat.service.MessagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversations")
@Slf4j
@AllArgsConstructor
@Tag(name = "会话/群组接口")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessagesService messagesService;

    @GetMapping
    @Operation(summary = "我的会话列表（群聊 + 已建立的私聊，含最后一条消息与未读数）")
    public Result<PageResult<ConversationVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                                                    @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询我的会话 userId={} page={} size={}", userId, page, size);
        return Result.success(conversationService.listMine(userId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "会话详情（需为会话成员）")
    public Result<ConversationVO> detail(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询会话详情 userId={} conversationId={}", userId, id);
        return Result.success(conversationService.detail(id, userId));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "会话成员列表（需为会话成员）")
    public Result<PageResult<User>> members(@PathVariable Long id,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询会话成员 userId={} conversationId={} page={} size={}", userId, id, page, size);
        return Result.success(conversationService.members(id, userId, page, size));
    }

    @PostMapping("/group")
    @Operation(summary = "创建群聊，返回新会话ID")
    public Result<Long> createGroup(@RequestBody CreateGroupDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("创建群聊 userId={} name={}", userId, dto.getName());
        return Result.success(conversationService.createGroup(userId, dto));
    }

    @PostMapping("/private")
    @Operation(summary = "创建 / 获取私聊会话，返回会话ID（仅限好友，已存在则返回已有会话）")
    public Result<Long> createPrivate(@RequestBody PrivateChatDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("创建/获取私聊会话 userId={} target={}", userId, dto.getTargetUserId());
        return Result.success(messagesService.createPrivateConversation(userId, dto.getTargetUserId()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "踢出成员（仅群主，不能踢自己）")
    public Result<Void> kick(@PathVariable Long id, @PathVariable Long userId) {
        Long operatorId = BaseContext.getCurrentId();
        log.info("踢出群成员 conversationId={} operator={} target={}", id, operatorId, userId);
        conversationService.kickMember(id, operatorId, userId);
        return Result.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改群信息（仅群主）")
    public Result<Void> updateGroup(@PathVariable Long id, @RequestBody UpdateGroupDTO dto) {
        Long operatorId = BaseContext.getCurrentId();
        log.info("修改群信息 conversationId={} operator={}", id, operatorId);
        conversationService.updateGroupInfo(id, operatorId, dto);
        return Result.success();
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "邀请成员加入群聊")
    public Result<Void> addMembers(@PathVariable Long id, @RequestBody MemberIdsDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("邀请成员入群 userId={} conversationId={}", userId, id);
        conversationService.addMembers(id, userId, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}/members/me")
    @Operation(summary = "退出群聊")
    public Result<Void> quit(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("退出群聊 userId={} conversationId={}", userId, id);
        conversationService.quit(id, userId);
        return Result.success();
    }
}
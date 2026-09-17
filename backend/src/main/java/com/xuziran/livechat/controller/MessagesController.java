package com.xuziran.livechat.controller;

import com.xuziran.livechat.common.context.BaseContext;
import com.xuziran.livechat.common.exception.BusinessException;
import com.xuziran.livechat.model.dto.MessageDTO;
import com.xuziran.livechat.model.dto.ReadMessageDTO;
import com.xuziran.livechat.model.vo.MessageVO;
import com.xuziran.livechat.model.vo.UnreadStat;
import com.xuziran.livechat.common.result.Result;
import com.xuziran.livechat.service.MessagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@Slf4j
@AllArgsConstructor
@Tag(name = "消息接口")
public class MessagesController {
    private final MessagesService messagesService;

    @GetMapping("/history")
    @Operation(summary = "会话历史（游标分页，仅会话成员可访问）")
    public Result<List<MessageVO>> getHistory(@RequestParam Long conversationId,
                                              @RequestParam(required = false) Long beforeId,
                                              @RequestParam(required = false) Integer limit) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询历史消息 userId={} conversationId={} beforeId={} limit={}", userId, conversationId, beforeId, limit);
        List<MessageVO> list = messagesService.queryByConversation(conversationId, beforeId, limit, userId);
        return Result.success(list);
    }

    @PostMapping("/read")
    @Operation(summary = "标记会话已读（推进游标，仅会话成员可操作）")
    public Result markRead(@RequestBody ReadMessageDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("标记已读 userId={} conversationId={}", userId, dto.getConversationId());
        messagesService.markRead(dto.getConversationId(), userId);
        return Result.success();
    }

    @PostMapping("/send")
    @Operation(summary = "HTTP 发送消息（兜底通道，需为会话成员，非成员返回业务失败）")
    public Result<MessageVO> send(@RequestBody MessageDTO dto) {
        Long userId = BaseContext.getCurrentId();
        if (dto == null || dto.getClientMsgId() == null || dto.getClientMsgId().isBlank()) {
            throw new BusinessException("clientMsgId 不能为空");
        }
        log.info("HTTP 发送消息 userId={} conversationId={}", userId, dto.getConversationId());
        MessageVO vo = messagesService.saveMessage(userId, dto.getConversationId(), dto);
        messagesService.broadcastMessage(vo);
        return Result.success(vo);
    }

    @GetMapping("/unread")
    @Operation(summary = "按会话统计未读数（当前登录用户）")
    public Result<List<UnreadStat>> unread() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询未读 userId={}", userId);
        return Result.success(messagesService.unread(userId));
    }
}
package com.xuziran.livechat.controller;

import com.xuziran.livechat.pojo.DTO.RelationshipDTO;
import com.xuziran.livechat.pojo.VO.MessageVO;
import com.xuziran.livechat.result.Result;
import com.xuziran.livechat.service.MessagesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@Slf4j
@Tag(name = "消息接口")
public class MessagesController {
    @Autowired
    private MessagesService messagesService;

    @GetMapping("/history")
    @Operation(summary = "查询历史消息")
    public Result<List<MessageVO>> getHistory() {
        log.info("查询历史消息");
        // 如果redis没有，从数据库中查询，将数据缓存到redis中
        List<MessageVO> list = messagesService.queryAll();
        return Result.success(list);
    }

    @PostMapping("/update")
    @Operation(summary = "更新消息")
    public Result update(@RequestBody RelationshipDTO relationshipDTO) {
        log.info("更新消息，清空redis");
        messagesService.update(relationshipDTO);
        return Result.success();
    }
}


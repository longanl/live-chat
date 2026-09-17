package com.xuziran.livechat.controller;

import com.xuziran.livechat.common.context.BaseContext;
import com.xuziran.livechat.common.result.Result;
import com.xuziran.livechat.model.dto.AddFriendDTO;
import com.xuziran.livechat.model.dto.RelationshipDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.UserVO;
import com.xuziran.livechat.service.FriendsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("friends")
@Slf4j
@AllArgsConstructor
@Tag(name = "好友相关接口")
public class FriendsController {
    private final FriendsService friendsService;

    @GetMapping("/list")
    @Operation(summary = "查询好友列表（当前登录用户）")
    public Result<List<User>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询好友列表 userId={}", userId);
        List<User> list = friendsService.list(userId);
        return Result.success(list);
    }
    @GetMapping("/require")
    @Operation(summary = "查询未处理的好友请求（当前登录用户）")
    public Result<List<User>> list0() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询未处理的好友请求 userId={}", userId);
        List<User> list = friendsService.listRequest(userId);
        return Result.success(list);
    }

    @PostMapping("/add")
    @Operation(summary = "添加好友")
    public Result add(@RequestBody AddFriendDTO addFriendDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("添加好友 userId={} friendUsername={}", userId, addFriendDTO.getFriendUsername());
        friendsService.add(userId, addFriendDTO.getFriendUsername());
        return Result.success();
    }
    @PostMapping("/approve")
    @Operation(summary = "审批好友请求")
    public Result approve(@RequestBody RelationshipDTO relationshipDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("审批好友请求 userId={} friendId={}", userId, relationshipDTO.getFriendId());
        friendsService.approve(userId, relationshipDTO.getFriendId());
        return Result.success();
    }
    @PostMapping("/reject")
    @Operation(summary = "拒绝好友请求")
    public Result reject(@RequestBody RelationshipDTO relationshipDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("拒绝好友请求 userId={} friendId={}", userId, relationshipDTO.getFriendId());
        friendsService.reject(userId, relationshipDTO.getFriendId());
        return Result.success();
    }
    @PostMapping("/delete")
    @Operation(summary = "删除好友")
    public Result delete(@RequestBody RelationshipDTO relationshipDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除好友 userId={} friendId={}", userId, relationshipDTO.getFriendId());
        friendsService.delete(userId, relationshipDTO.getFriendId());
        return Result.success();
    }

    @PostMapping("/block")
    @Operation(summary = "拉黑用户")
    public Result block(@RequestBody RelationshipDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("拉黑 userId={} target={}", userId, dto.getFriendId());
        friendsService.block(userId, dto.getFriendId());
        return Result.success();
    }

    @DeleteMapping("/block/{userId}")
    @Operation(summary = "取消拉黑")
    public Result unblock(@PathVariable Long userId) {
        Long me = BaseContext.getCurrentId();
        log.info("取消拉黑 me={} target={}", me, userId);
        friendsService.unblock(me, userId);
        return Result.success();
    }

    @GetMapping("/blocked")
    @Operation(summary = "拉黑列表（分页）")
    public Result<PageResult<UserVO>> blocked(@RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "20") Integer size) {
        Long me = BaseContext.getCurrentId();
        log.info("拉黑列表 me={} page={} size={}", me, page, size);
        return Result.success(friendsService.blockedList(me, page, size));
    }
}
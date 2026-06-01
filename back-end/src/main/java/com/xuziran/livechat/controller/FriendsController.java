package com.xuziran.livechat.controller;

import com.xuziran.livechat.pojo.DTO.AddFriendDTO;
import com.xuziran.livechat.pojo.DTO.RelationshipDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.pojo.entity.UserRelations;
import com.xuziran.livechat.result.Result;
import com.xuziran.livechat.service.FriendsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// TODO 改成websocket请求
@RestController
@RequestMapping("friends")
@Slf4j
@Tag(name = "好友相关接口")
public class FriendsController {
    @Autowired
    private FriendsService friendsService;


    @GetMapping("/list/{id}")
    @Operation(summary = "查询好友列表")
    public Result<List<User>> list(@PathVariable Long id) {
        log.info("查询好友列表啊啊啊啊啊啊");
        List<User> list = friendsService.list(id);
        return Result.success(list);
    }
    @GetMapping("/require/{id}")
    @Operation(summary = "查询未处理的好友请求")
    public Result<List<User>> list0(@PathVariable Long id) {
        log.info("查询未处理的好友请求");
        List<User> list = friendsService.listRequest(id);
        return Result.success(list);
    }



    @PostMapping("/add")
    @Operation(summary = "添加好友")
    public Result add(@RequestBody AddFriendDTO addFriendDTO) {
        log.info("添加好友：{}", addFriendDTO);
        friendsService.add(addFriendDTO);
        return Result.success();
    }
    @PostMapping("/approve")
    @Operation(summary = "审批好友请求")
    public Result approve(@RequestBody RelationshipDTO relationshipDTO) {
        log.info("审批好友请求：{}", relationshipDTO);
        friendsService.approve(relationshipDTO.getUserId(), relationshipDTO.getFriendId());
        return Result.success();
    }
    @PostMapping("/reject")
    @Operation(summary = "拒绝好友请求")
    public Result reject(@RequestBody RelationshipDTO relationshipDTO) {
        log.info("拒绝好友请求：{}", relationshipDTO);
        friendsService.reject(relationshipDTO.getUserId(), relationshipDTO.getFriendId());
        return Result.success();
    }
    @PostMapping("/delete")
    @Operation(summary = "删除好友")
    public Result delete(@RequestBody RelationshipDTO relationshipDTO) {
        log.info("删除好友：{}", relationshipDTO);
        friendsService.delete(relationshipDTO.getUserId(), relationshipDTO.getFriendId());
        return Result.success();
    }
}

package com.xuziran.livechat.service;

import com.xuziran.livechat.pojo.DTO.AddFriendDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.pojo.entity.UserRelations;

import java.util.List;

public interface FriendsService {
    void add(AddFriendDTO addFriendDTO);

    List<User> list(Long id);

    void approve(Long userId,Long friendId);

    void reject(Long userId,Long friendId);

    List<User> listRequest(Long id);

    void delete(Long userId,Long friendId);
}

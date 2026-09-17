package com.xuziran.livechat.service;

import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.UserVO;

import java.util.List;

public interface FriendsService {
    void add(Long userId, String friendUsername);

    List<User> list(Long id);

    void approve(Long userId, Long friendId);

    void reject(Long userId, Long friendId);

    List<User> listRequest(Long id);

    void delete(Long userId, Long friendId);

    /** 拉黑（仅限好友，blockerId=当前用户） */
    void block(Long userId, Long targetId);

    /** 取消拉黑 */
    void unblock(Long userId, Long targetId);

    /** 拉黑列表（分页） */
    PageResult<UserVO> blockedList(Long userId, Integer page, Integer size);
}
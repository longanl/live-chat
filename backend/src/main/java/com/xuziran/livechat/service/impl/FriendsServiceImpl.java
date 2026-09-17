package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.common.exception.BusinessException;
import com.xuziran.livechat.mapper.FriendsMapper;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.UserVO;
import com.xuziran.livechat.service.FriendsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class FriendsServiceImpl implements FriendsService {
    private final FriendsMapper friendsMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<User> list(Long id, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1 || size > 100) size = 20;
        Long total = friendsMapper.countList(id);
        if (total == null || total == 0) {
            return PageResult.<User>of(0L, page, size, Collections.emptyList());
        }
        int offset = (page - 1) * size;
        List<User> list = friendsMapper.list(id, offset, size);
        return PageResult.of(total, page, size, list);
    }

    @Override
    public List<User> listRequest(Long id) {
        return friendsMapper.listRequest(id);
    }

    @Override
    @Transactional
    public void add(Long userId, String friendUsername) {
        Long friendId = userMapper.getIdByUsername(friendUsername);
        if (friendId == null) {
            throw new BusinessException("用户不存在");
        }
        if (userId.equals(friendId)) {
            throw new BusinessException("不能添加自己为好友");
        }
        if (isFriend(userId, friendId)) {
            throw new BusinessException("你们已经是好友了");
        }
        // 对方已经先发起了申请：直接同意入好友
        if (friendsMapper.countPendingRequest(friendId, userId) > 0) {
            friendsMapper.confirmRequest(friendId, userId);
            addRelation(userId, friendId);
            return;
        }
        // 我已有待处理申请
        if (friendsMapper.countPendingRequest(userId, friendId) > 0) {
            throw new BusinessException("申请已发送，请等待对方确认");
        }
        friendsMapper.addRequest(userId, friendId);
    }

    @Override
    @Transactional
    public void approve(Long userId, Long friendId) {
        int updated = friendsMapper.confirmRequest(friendId, userId);
        if (updated == 0) {
            throw new BusinessException("没有待处理的申请");
        }
        addRelation(userId, friendId);
    }

    @Override
    @Transactional
    public void reject(Long userId, Long friendId) {
        int updated = friendsMapper.rejectRequest(friendId, userId);
        if (updated == 0) {
            throw new BusinessException("没有待处理的申请");
        }
    }

    @Override
    @Transactional
    public void delete(Long userId, Long friendId) {
        friendsMapper.deleteRelation(lowId(userId, friendId), highId(userId, friendId));
        friendsMapper.invalidateRequests(userId, friendId);
    }

    @Override
    @Transactional
    public void block(Long userId, Long targetId) {
        if (targetId == null) {
            throw new BusinessException("参数错误");
        }
        if (userId.equals(targetId)) {
            throw new BusinessException("不能拉黑自己");
        }
        if (friendsMapper.countBlock(userId, targetId) > 0) {
            throw new BusinessException("已拉黑该用户");
        }
        friendsMapper.insertBlock(userId, targetId);
    }

    @Override
    @Transactional
    public void unblock(Long userId, Long targetId) {
        if (targetId == null) {
            throw new BusinessException("参数错误");
        }
        int deleted = friendsMapper.deleteBlock(userId, targetId);
        if (deleted == 0) {
            throw new BusinessException("未拉黑该用户");
        }
    }

    @Override
    public PageResult<UserVO> blockedList(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 20;
        }
        Long total = friendsMapper.countBlocked(userId);
        if (total == null || total == 0) {
            return PageResult.<UserVO>of(0L, page, size, Collections.emptyList());
        }
        int offset = (page - 1) * size;
        List<UserVO> list = friendsMapper.selectBlockedPage(userId, offset, size);
        return PageResult.<UserVO>of(total, page, size, list);
    }

    /** 好友关系行固定 user_id < friend_id，归一化后写入 */
    private void addRelation(Long a, Long b) {
        friendsMapper.insertRelation(lowId(a, b), highId(a, b));
    }

    private long lowId(Long a, Long b) {
        return Math.min(a, b);
    }

    private long highId(Long a, Long b) {
        return Math.max(a, b);
    }

    private boolean isFriend(Long a, Long b) {
        return friendsMapper.countRelation(lowId(a, b), highId(a, b)) > 0;
    }
}
package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.mapper.FriendsMapper;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.pojo.DTO.AddFriendDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendsServiceImpl implements FriendsService {
    @Autowired
    private FriendsMapper friendsMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> list(Long id) {
        List<User> list = friendsMapper.list(id);
        return list;
    }
    @Override
    public List<User> listRequest(Long id) {
        List<User> list = friendsMapper.listRequest(id);
        return list;
    }

    @Override
    @Transactional
    public void add(AddFriendDTO addFriendDTO) {
        Long userId = addFriendDTO.getUserId();
        Long friendId=userMapper.getIdByUsername(addFriendDTO.getFriendUsername());
        //创建添加请求
        friendsMapper.insert(userId,friendId);
    }

    @Override
    @Transactional
    public void approve(Long userId,Long friendId) {
        friendsMapper.update(userId,friendId,1);
    }

    @Override
    @Transactional
    public void reject(Long userId,Long friendId) {
        friendsMapper.update(userId,friendId,2);
    }


    @Override
    public void delete(Long userId,Long friendId) {
        friendsMapper.delete(userId,friendId);
    }
}

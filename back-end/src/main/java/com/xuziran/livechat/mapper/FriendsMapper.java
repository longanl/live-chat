package com.xuziran.livechat.mapper;

import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.pojo.entity.UserRelations;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendsMapper {
    void insert(Long userId,Long friendId);

    List<User> list(Long userId);

    void update(Long userId,Long friendId,Integer status);

    List<User> listRequest(Long userId);

    void delete(Long userId, Long friendId);
}

package com.xuziran.livechat.mapper;

import com.xuziran.livechat.pojo.DTO.PasswordDTO;
import com.xuziran.livechat.pojo.DTO.RegisterDTO;
import com.xuziran.livechat.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User login(String username);

    void insert(RegisterDTO registerDTO);

    List<User> queryOnlineUser();

    void updateStatus(User user);

    Long getIdByUsername(String username);

    void update(User user);

    User getById(Long id);

    Long count();
}

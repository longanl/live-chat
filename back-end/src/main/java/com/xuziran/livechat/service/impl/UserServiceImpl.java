package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.exception.PasswordErrorException;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.pojo.DTO.PasswordDTO;
import com.xuziran.livechat.pojo.DTO.ProfileDTO;
import com.xuziran.livechat.pojo.DTO.RegisterDTO;
import com.xuziran.livechat.pojo.DTO.UserDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.service.UserService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User login(UserDTO userDTO) throws AccountNotFoundException, PasswordErrorException {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        User user = userMapper.login(username);
        //处理各种异常情况（用户名不存在、密码不对）
        if (user == null) {
            //账号不存在
            throw new AccountNotFoundException("用户不存在");
        }
        //密码比对
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException("密码错误");
        }
        //登入成功
        log.info("用户：{} 登入成功", username);
        user.setStatus(1);
        userMapper.update(user);
        return user;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        userMapper.insert(registerDTO);
    }

    @Override
    public void updatePassword(PasswordDTO passwordDTO) throws PasswordErrorException {
        User user = userMapper.getById(passwordDTO.getId());
        if (!passwordDTO.getOldPassword().equals(user.getPassword())) {
            throw new PasswordErrorException("旧密码错误");
        }
        user.setId(passwordDTO.getId());
        user.setPassword(passwordDTO.getNewPassword());
        userMapper.update(user);
    }

    @Override
    public void logout(Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(0);
        userMapper.update(user);
    }

    @Override
    public void updateProfile(ProfileDTO profileDTO) {
        User user = new User();
        BeanUtils.copyProperties(profileDTO, user);
        userMapper.update(user);
    }
}

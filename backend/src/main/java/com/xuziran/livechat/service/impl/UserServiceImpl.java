package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.common.exception.BusinessException;
import com.xuziran.livechat.common.exception.PasswordErrorException;
import com.xuziran.livechat.mapper.MessagesMapper;
import com.xuziran.livechat.mapper.UserMapper;
import com.xuziran.livechat.model.dto.PasswordDTO;
import com.xuziran.livechat.model.dto.ProfileDTO;
import com.xuziran.livechat.model.dto.RegisterDTO;
import com.xuziran.livechat.model.dto.UserDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.UserDetailVO;
import com.xuziran.livechat.model.vo.UserSearchVO;
import com.xuziran.livechat.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final MessagesMapper messagesMapper;

    /** 内置群会话 ID（init.sql 固定为 1） */
    private static final Long GROUP_CONVERSATION_ID = 1L;
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
    @Transactional
    public void register(RegisterDTO registerDTO) {
        userMapper.insert(registerDTO);
        // 新用户自动加入内置群会话
        if (registerDTO.getId() != null) {
            messagesMapper.insertMember(GROUP_CONVERSATION_ID, registerDTO.getId());
        }
    }

    @Override
    public void updatePassword(Long userId, PasswordDTO passwordDTO) throws PasswordErrorException {
        User user = userMapper.getById(userId);
        if (!passwordDTO.getOldPassword().equals(user.getPassword())) {
            throw new PasswordErrorException("旧密码错误");
        }
        user.setId(userId);
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
    public void updateProfile(Long userId, ProfileDTO profileDTO) {
        User user = new User();
        BeanUtils.copyProperties(profileDTO, user);
        user.setId(userId);
        userMapper.update(user);
    }

    @Override
    public PageResult<UserSearchVO> searchUsers(String keyword, Integer page, Integer size, Long currentUserId) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException("搜索关键词不能为空");
        }
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 20;
        }
        String kw = keyword.trim();
        Long total = userMapper.countSearch(kw, currentUserId);
        if (total == null || total == 0) {
            return PageResult.<UserSearchVO>of(0L, page, size, Collections.emptyList());
        }
        int offset = (page - 1) * size;
        List<UserSearchVO> list = userMapper.selectSearch(kw, currentUserId, offset, size);
        return PageResult.<UserSearchVO>of(total, page, size, list);
    }

    @Override
    public UserDetailVO getUserDetail(Long userId, Long currentUserId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        UserDetailVO detail = userMapper.selectUserDetail(userId, currentUserId);
        if (detail == null) {
            throw new BusinessException("用户不存在");
        }
        return detail;
    }
}

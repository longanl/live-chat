package com.xuziran.livechat.service;

import com.xuziran.livechat.common.exception.PasswordErrorException;
import com.xuziran.livechat.model.dto.PasswordDTO;
import com.xuziran.livechat.model.dto.ProfileDTO;
import com.xuziran.livechat.model.dto.RegisterDTO;
import com.xuziran.livechat.model.dto.UserDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.UserDetailVO;
import com.xuziran.livechat.model.vo.UserSearchVO;

import javax.security.auth.login.AccountNotFoundException;

public interface UserService {
    User login(UserDTO userDTO) throws AccountNotFoundException, PasswordErrorException;

    void register(RegisterDTO registerDTO);

    void updatePassword(Long userId, PasswordDTO passwordDTO) throws PasswordErrorException;

    void logout(Long id);

    void updateProfile(Long userId, ProfileDTO profileDTO);

    /** 搜索用户（分页，含当前用户视角的关系字段） */
    PageResult<UserSearchVO> searchUsers(String keyword, Integer page, Integer size, Long currentUserId);

    /** 用户详情（含当前用户视角的关系字段），不存在时抛业务异常 */
    UserDetailVO getUserDetail(Long userId, Long currentUserId);
}

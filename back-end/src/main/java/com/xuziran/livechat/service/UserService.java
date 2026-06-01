package com.xuziran.livechat.service;

import com.xuziran.livechat.exception.PasswordErrorException;
import com.xuziran.livechat.pojo.DTO.PasswordDTO;
import com.xuziran.livechat.pojo.DTO.ProfileDTO;
import com.xuziran.livechat.pojo.DTO.RegisterDTO;
import com.xuziran.livechat.pojo.DTO.UserDTO;
import com.xuziran.livechat.pojo.entity.User;

import javax.security.auth.login.AccountNotFoundException;

public interface UserService {
    User login(UserDTO userDTO) throws AccountNotFoundException, PasswordErrorException;

    void register(RegisterDTO registerDTO);

    void updatePassword(PasswordDTO passwordDTO) throws PasswordErrorException;

    void logout(Long id);

    void updateProfile(ProfileDTO profileDTO);
}

package com.xuziran.livechat.controller;

import com.xuziran.livechat.exception.PasswordErrorException;
import com.xuziran.livechat.pojo.DTO.PasswordDTO;
import com.xuziran.livechat.pojo.DTO.ProfileDTO;
import com.xuziran.livechat.pojo.DTO.RegisterDTO;
import com.xuziran.livechat.pojo.DTO.UserDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.pojo.VO.UserVO;
import com.xuziran.livechat.properties.JwtProperties;
import com.xuziran.livechat.result.Result;
import com.xuziran.livechat.service.UserService;
import com.xuziran.livechat.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/users")
@Slf4j
@Tag(name = "用户相关接口", description = "用户相关接口")
public class UsersController {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserService userService;



    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        log.info("注册：{}", registerDTO);
        userService.register(registerDTO);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody UserDTO userDTO) throws PasswordErrorException, AccountNotFoundException {
        log.info("登录：{}", userDTO);
        User user = userService.login(userDTO);
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims);
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .updateTime(LocalDateTime.now())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .token(token)
                .rememberMe(userDTO.getRememberMe())
                .build();
        return Result.success(userVO);
    }
    //修改密码
    @Operation(summary = "修改密码")
    @PutMapping("/modifyPassword")
    public Result updatePassword(@RequestBody PasswordDTO passwordDTO) throws PasswordErrorException {
        log.info("修改密码：{}", passwordDTO);
        userService.updatePassword(passwordDTO);
        return Result.success();
    }
    @Operation(summary = "用户登出")
    @GetMapping("/logout/{id}")
    public Result logout(@PathVariable Long id) {
        log.info("用户：" + id + "下线");
        userService.logout(id);
        return Result.success();
    }

    @PostMapping("/updateProfile")
    @Operation(summary = "更新用户信息")
    public Result updateProfile(@RequestBody ProfileDTO profileDTO) {
        log.info("更新用户信息：{}", profileDTO);
        userService.updateProfile(profileDTO);
        return Result.success();
    }


}

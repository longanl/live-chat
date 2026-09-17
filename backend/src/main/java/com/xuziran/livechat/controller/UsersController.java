package com.xuziran.livechat.controller;

import com.xuziran.livechat.common.exception.PasswordErrorException;
import com.xuziran.livechat.common.context.BaseContext;
import com.xuziran.livechat.model.dto.PasswordDTO;
import com.xuziran.livechat.model.dto.ProfileDTO;
import com.xuziran.livechat.model.dto.RegisterDTO;
import com.xuziran.livechat.model.dto.UserDTO;
import com.xuziran.livechat.model.entity.User;
import com.xuziran.livechat.model.vo.PageResult;
import com.xuziran.livechat.model.vo.UserDetailVO;
import com.xuziran.livechat.model.vo.UserSearchVO;
import com.xuziran.livechat.model.vo.UserVO;
import com.xuziran.livechat.common.properties.JwtProperties;
import com.xuziran.livechat.common.result.Result;
import com.xuziran.livechat.service.UserService;
import com.xuziran.livechat.common.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@AllArgsConstructor
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户相关接口", description = "用户相关接口")
public class UsersController {
    private final JwtProperties jwtProperties;
    private final UserService userService;



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
        Long userId = BaseContext.getCurrentId();
        log.info("修改密码 userId={}", userId);
        userService.updatePassword(userId, passwordDTO);
        return Result.success();
    }
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result logout() {
        Long userId = BaseContext.getCurrentId();
        log.info("用户：" + userId + "下线");
        userService.logout(userId);
        return Result.success();
    }

    @PostMapping("/updateProfile")
    @Operation(summary = "更新用户信息")
    public Result updateProfile(@RequestBody ProfileDTO profileDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("更新用户信息 userId={}", userId);
        userService.updateProfile(userId, profileDTO);
        return Result.success();
    }

    @Operation(summary = "搜索用户（分页）")
    @GetMapping("/search")
    public Result<PageResult<UserSearchVO>> search(@RequestParam String keyword,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        Long userId = BaseContext.getCurrentId();
        log.info("搜索用户 keyword={} page={} size={}", keyword, page, size);
        return Result.success(userService.searchUsers(keyword, page, size, userId));
    }

    @Operation(summary = "查看用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVO> userDetail(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("查看用户详情 userId={} target={}", userId, id);
        return Result.success(userService.getUserDetail(id, userId));
    }


}

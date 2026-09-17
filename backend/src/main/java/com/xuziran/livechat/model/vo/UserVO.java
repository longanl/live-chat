package com.xuziran.livechat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String avatar;
    private String nickname;
    @Schema(allowableValues = {"0", "1"})
    private Integer status;
    private LocalDateTime updateTime;
    private String token;
    private Boolean rememberMe;
}

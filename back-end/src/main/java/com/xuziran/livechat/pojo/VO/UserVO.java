package com.xuziran.livechat.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer status;
    private LocalDateTime updateTime;
    private String token;
    private Boolean rememberMe;
}

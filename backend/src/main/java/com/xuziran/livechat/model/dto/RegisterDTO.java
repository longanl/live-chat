package com.xuziran.livechat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    private String username;
    private String nickname;
    private String password;
    private String confirmPassword;
    private String avatar;
    /** 注册成功后由库返回的用户ID（useGeneratedKeys 回填），客户端不应传 */
    @JsonIgnore
    private Long id;
}

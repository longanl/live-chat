package com.xuziran.livechat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User  implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String avatar;
    private String nickname;
    private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  /** 好友列表回传：与该好友的私聊会话ID（未聊过为 null），不属于 users 表列 */
  private Long conversationId;
  /** 个性签名 */
  private String signature;
}

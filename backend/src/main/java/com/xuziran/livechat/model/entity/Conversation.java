package com.xuziran.livechat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 1-私聊 2-群聊 */
    private Integer type;
    /** 群聊名称；私聊为空 */
    private String name;
  /** 群主用户ID；私聊为空 */
  private Long ownerId;
  /** 群头像 */
  private String avatar;
  /** 群公告 */
  private String notice;
  private LocalDateTime createTime;
}
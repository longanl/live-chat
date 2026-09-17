package com.xuziran.livechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 创建 / 获取私聊会话请求 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChatDTO {
    /** 对方用户ID */
    private Long targetUserId;
}
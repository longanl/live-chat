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
public class ConversationMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 所属会话ID */
    private Long conversationId;
    /** 成员用户ID */
    private Long userId;
    /** 已读游标：已读到的最大消息id */
    private Long lastReadMsgId;
    /** 加入时间 */
    private LocalDateTime joinTime;
}
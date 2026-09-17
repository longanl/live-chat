package com.xuziran.livechat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private Long id;
    /** 所属会话ID */
    private Long conversationId;
    /** 发送者ID */
    private Long senderId;
    /** 1-文本 2-文件 */
    @Schema(allowableValues = {"1", "2"})
    private Integer messageType;
    /** 文本正文（文件消息可为说明文字） */
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    /** image / video / file */
    private String fileType;
    private LocalDateTime sendTime;
    /** 客户端幂等UUID（用于去重） */
    private String clientMsgId;
    /** 是否已撤回（0=否，1=是） */
    private Integer recalled;
}
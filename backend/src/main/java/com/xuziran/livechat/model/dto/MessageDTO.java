package com.xuziran.livechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
    private Long senderId;
    private Long receiverId;
    /**
     * 目标会话ID（群聊必传）
     * 为空时群聊回落到内置群会话，兼容旧客户端。
     */
    private Long conversationId;
    private String content;
    /** 1-文本 2-文件（图片/视频/文件） */
    private Integer messageType;
    /** 文件可访问 URL */
    private String fileUrl;
    /** 原始文件名 */
    private String fileName;
    /** 文件大小（字节） */
    private Long fileSize;
    /** image / video / file */
    private String fileType;
    /** 客户端生成 UUID，HTTP /messages/send 必填用于幂等标识（当前仅校验必填，去重待后续实现） */
    private String clientMsgId;
}

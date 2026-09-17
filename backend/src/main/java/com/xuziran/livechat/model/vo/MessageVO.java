package com.xuziran.livechat.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class MessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 所属会话ID */
    private Long conversationId;
    private Long senderId;
    private String nickname;
    private String avatar;
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
}
package com.xuziran.livechat.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话视图对象：用于「我的会话列表」与「会话详情」。
 *
 * 私聊：name 为空，由 peer* 字段表达对方；群聊：peer* 为空，由 name/ownerId 表达群信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long conversationId;
    /** 1-私聊 2-群聊 */
    @Schema(allowableValues = {"1", "2"})
    private Integer type;
    /** 群聊名称；私聊为空 */
    private String name;
    /** 群主用户ID；私聊为空 */
    private Long ownerId;
    /** 群头像 */
    private String avatar;
    /** 群公告 */
    private String notice;
    /** 成员总数 */
    private Integer memberCount;
    private LocalDateTime createTime;

    // ---- 私聊对方信息（type=1 时有值）----
    private Long peerId;
    private String peerNickname;
    private String peerAvatar;
    private Integer peerStatus;

    // ---- 最后一条消息（会话尚无消息时为空）----
    private Long lastMessageId;
    private String lastContent;
    /** 1-文本 2-文件 */
    @Schema(allowableValues = {"1", "2"})
    private Integer lastMessageType;
    /** 文件消息的原始文件名（用于列表预览） */
    private String lastFileName;
    private String lastSenderNickname;
    private LocalDateTime lastSendTime;

    /** 当前登录用户在该会话的未读数 */
    private Integer unreadCount;
}

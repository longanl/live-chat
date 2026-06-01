package com.xuziran.livechat.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Integer messageType;
    private String content;
    private Long fileId;
    private LocalDateTime sendTime;
    private Integer readStatus;
}

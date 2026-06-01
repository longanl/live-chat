package com.xuziran.livechat.pojo.VO;

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
public class MessageVO  implements Serializable{
    // 生成序列化版本号（建议添加，避免序列化兼容性问题）
    private static final long serialVersionUID = 1L;


    private Long id;
    private String nickname;
    private String avatar;
    private Long senderId;
    private Long receiverId;
    private Integer messageType;
    private String content;
    private Long fileId;
    private LocalDateTime sendTime;
    private Integer readStatus;
}

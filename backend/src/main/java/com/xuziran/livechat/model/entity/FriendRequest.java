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
public class FriendRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 申请人ID */
    private Long fromUserId;
    /** 接收人ID */
    private Long toUserId;
    /** 0-待处理 1-已同意 2-已拒绝(可再次申请) */
    @Schema(allowableValues = {"0", "1", "2"})
    private Integer status;
    /** 处理时间 */
    private LocalDateTime handleTime;
    /** 申请时间 */
    private LocalDateTime createTime;
}
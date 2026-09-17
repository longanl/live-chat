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
public class FriendRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 好友对中较小的一方 */
    private Long userId;
    /** 好友对中较大的一方 */
    private Long friendId;
    /** 建立时间 */
    private LocalDateTime createTime;
}
package com.xuziran.livechat.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRelations {
    private Long id;
    private Long userId;
    private Long friendId;
    private Integer status;
    private LocalDateTime createTime;
}

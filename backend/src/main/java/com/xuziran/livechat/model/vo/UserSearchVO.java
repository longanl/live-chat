package com.xuziran.livechat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户搜索结果（含当前用户视角的关系字段） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    /**
     * NONE / FRIEND / REQUEST_SENT / REQUEST_RECEIVED
     * （BLOCKED 依赖拉黑功能，暂未实现，见 API 文档）
     */
    private String relation;
}
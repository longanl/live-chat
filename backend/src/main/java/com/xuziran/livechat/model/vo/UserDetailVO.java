package com.xuziran.livechat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户详情（含当前用户视角的关系字段） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer status;
    /** 个性签名 */
    private String signature;
    /** NONE / FRIEND / REQUEST_SENT / REQUEST_RECEIVED */
    private String relation;
}
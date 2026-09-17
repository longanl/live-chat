package com.xuziran.livechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** 创建群聊请求体 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    /** 初始成员用户ID（不含创建者，创建者自动成为群主与成员） */
    private List<Long> memberIds;
}

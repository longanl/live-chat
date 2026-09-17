package com.xuziran.livechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** 群聊成员批量操作请求体（邀请入群） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberIdsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Long> userIds;
}

package com.xuziran.livechat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 修改群信息请求 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupDTO {
    private String name;
    private String avatar;
    private String notice;
}
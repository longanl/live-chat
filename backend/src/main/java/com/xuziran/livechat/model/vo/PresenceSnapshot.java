package com.xuziran.livechat.model.vo;

import com.xuziran.livechat.model.vo.OnlineUserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 在线状态快照：新连接建立时定向推送给该用户，避免订阅竞态丢失初始状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 在线用户列表 */
    private List<OnlineUserDTO> users;
    /** 在线人数 */
    private Integer count;
}
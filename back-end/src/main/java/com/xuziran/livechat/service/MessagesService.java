package com.xuziran.livechat.service;

import com.xuziran.livechat.pojo.DTO.RelationshipDTO;
import com.xuziran.livechat.pojo.VO.MessageVO;

import java.util.List;

public interface MessagesService {
    List<MessageVO> queryAll();

    void update(RelationshipDTO relationshipDTO);
}

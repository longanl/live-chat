package com.xuziran.livechat.mapper;

import com.xuziran.livechat.pojo.DTO.RelationshipDTO;
import com.xuziran.livechat.pojo.entity.User;
import com.xuziran.livechat.pojo.VO.MessageVO;

import java.util.List;

public interface MessagesMapper {
    List<MessageVO> queryAll();
    User queryUserById(Long id);

    void insertMessage(MessageVO messageVO);

    void update(RelationshipDTO relationshipDTO);
}

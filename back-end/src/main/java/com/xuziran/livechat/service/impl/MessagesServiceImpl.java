package com.xuziran.livechat.service.impl;

import com.xuziran.livechat.mapper.MessagesMapper;
import com.xuziran.livechat.pojo.DTO.RelationshipDTO;
import com.xuziran.livechat.pojo.VO.MessageVO;
import com.xuziran.livechat.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagesServiceImpl implements MessagesService {
    @Autowired
    private MessagesMapper messagesMapper;
    @Override
    public List<MessageVO> queryAll() {
        List<MessageVO> list = messagesMapper.queryAll();
        return list;
    }

    @Override
    public void update(RelationshipDTO relationshipDTO) {
        messagesMapper.update(relationshipDTO);
    }
}

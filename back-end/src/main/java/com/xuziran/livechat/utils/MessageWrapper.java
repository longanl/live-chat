package com.xuziran.livechat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuziran.livechat.json.JacksonObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class MessageWrapper<T> {
    private String type;
    private T content;
}

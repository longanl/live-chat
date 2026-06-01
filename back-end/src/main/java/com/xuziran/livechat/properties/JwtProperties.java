package com.xuziran.livechat.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "live-chat.jwt")
@Data
public class JwtProperties {
    private String SecretKey;
    private long Ttl;
    private String TokenName;
}

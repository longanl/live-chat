package com.xuziran.livechat;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@EnableTransactionManagement //开启注解方式的事务管理
@EnableCaching
@EnableScheduling
@MapperScan("com.xuziran.livechat.mapper") // 指定Mapper接口所在的包
public class LiveChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveChatApplication.class, args);
        log.info("启动成功");
    }

}

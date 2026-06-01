package com.xuziran.livechat.utils;

import com.xuziran.livechat.properties.FileProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@Slf4j
public class LocalFileUtil {

    @Autowired
    private FileProperties fileProperties;

    public String upload(byte[] bytes, String originalFilename) throws IOException {
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = originalFilename.substring(dotIndex);
        }
        String newName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dest = Path.of(fileProperties.getPath(), newName);
        Files.createDirectories(dest.getParent());
        Files.write(dest, bytes);
        String url = fileProperties.getDomain() + newName;
        log.info("文件上传到本地: {}", url);
        return url;
    }
}

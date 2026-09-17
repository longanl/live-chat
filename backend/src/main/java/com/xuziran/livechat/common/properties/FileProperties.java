package com.xuziran.livechat.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "upload")
public class FileProperties {
    /** 本地存储根目录（Nginx 图床挂载目录） */
    private String path;
    /** 对外访问域名前缀，必须以 / 结尾，例如 https://img.example.com/ */
    private String domain;
    /** 聊天文件的二级目录，最终为 {baseDir}/{type}/yyyy/MM/dd/ */
    private String baseDir = "chat";
    /** 头像二级目录，最终为 {avatarDir}/yyyy/MM/dd/ */
    private String avatarDir = "user";
    /** 图片允许的扩展名 */
    private String imageExtensions = "jpg,jpeg,png,gif,webp";
    /** 视频允许的扩展名 */
    private String videoExtensions = "mp4,webm,mov";
    /** 普通文件允许的扩展名 */
    private String fileExtensions = "pdf,zip,doc,docx,xls,xlsx,txt,ppt,pptx";
    /** 图片大小上限（字节） */
    private long maxImageSize = 10 * 1024 * 1024;
    /** 视频大小上限（字节） */
    private long maxVideoSize = 200L * 1024 * 1024;
    /** 普通文件大小上限（字节） */
    private long maxFileSize = 50L * 1024 * 1024;
}

package com.xuziran.livechat.common.utils;

import com.xuziran.livechat.model.vo.UploadResult;
import com.xuziran.livechat.common.properties.FileProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 本地文件存储，文件写入 Nginx 图床目录，由 Nginx 对外提供访问。
 */
@Component
@Slf4j
@AllArgsConstructor
public class LocalFileUtil {

    private static final Set<String> SUPPORTED_TYPES = Set.of("avatar", "image", "video", "file");
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileProperties fileProperties;

    /**
     * 上传文件
     *
     * @param bytes            文件内容
     * @param originalFilename 原始文件名
     * @param type             avatar / image / video / file
     * @return 上传结果（含公网 URL）
     */
    public UploadResult upload(byte[] bytes, String originalFilename, String type) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        if (type == null || !SUPPORTED_TYPES.contains(type)) {
            throw new IllegalArgumentException("不支持的文件类型: " + type);
        }
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("文件缺少有效扩展名");
        }
        String ext = originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);

        if (!allowedExtensions(type).contains(ext)) {
            throw new IllegalArgumentException("不支持的文件扩展名: " + ext);
        }
        long maxSize = maxSize(type);
        if (bytes.length > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制（最大 " + (maxSize / 1024 / 1024) + "MB）");
        }

        String category = "avatar".equals(type)
                ? fileProperties.getAvatarDir()
                : fileProperties.getBaseDir() + "/" + type;
        String relativeDir = category + "/" + LocalDate.now().format(DATE_DIR);
        String newName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relativeUrl = relativeDir + "/" + newName;

        Path root = Path.of(fileProperties.getPath()).toAbsolutePath().normalize();
        Path dest = root.resolve(relativeUrl).normalize();
        if (!dest.startsWith(root)) {
            throw new IllegalArgumentException("非法的文件路径");
        }
        Files.createDirectories(dest.getParent());
        Files.write(dest, bytes);

        String url = buildUrl(relativeUrl);
        log.info("文件上传到本地: {} ({} bytes, type={})", url, bytes.length, type);
        return UploadResult.builder()
                .url(url)
                .name(originalFilename)
                .size((long) bytes.length)
                .ext(ext)
                .type(type)
                .build();
    }

    private String buildUrl(String relativeUrl) {
        String domain = fileProperties.getDomain();
        if (domain == null || domain.isBlank()) {
            domain = "/";
        }
        if (!domain.endsWith("/")) {
            domain += "/";
        }
        return domain + relativeUrl;
    }

    private Set<String> allowedExtensions(String type) {
        String raw = switch (type) {
            case "video" -> fileProperties.getVideoExtensions();
            case "file" -> fileProperties.getFileExtensions();
            default -> fileProperties.getImageExtensions();
        };
        return Arrays.stream(raw.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private long maxSize(String type) {
        return switch (type) {
            case "video" -> fileProperties.getMaxVideoSize();
            case "file" -> fileProperties.getMaxFileSize();
            default -> fileProperties.getMaxImageSize();
        };
    }
}

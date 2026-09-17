package com.xuziran.livechat.controller;

import com.xuziran.livechat.model.vo.UploadResult;
import com.xuziran.livechat.common.properties.JwtProperties;
import com.xuziran.livechat.common.result.Result;
import com.xuziran.livechat.common.utils.JwtUtil;
import com.xuziran.livechat.common.utils.LocalFileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@AllArgsConstructor
@Slf4j
@Tag(name = "文件上传相关接口")
public class FileController {

    private final LocalFileUtil localFileUtil;

    private final JwtProperties jwtProperties;

    /**
     * 通用上传接口：图片 / 视频 / 文件（需登录）
     */
    @Operation(summary = "上传聊天文件（图片/视频/文件）")
    @PostMapping("/files/upload")
    public Result<UploadResult> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "type", defaultValue = "file") String type,
                                       @RequestHeader(value = "token", required = false) String token,
                                       HttpServletResponse response) {
        if (!checkAuth(type, token, response)) {
            return Result.error("未登录或登录已过期");
        }
        try {
            UploadResult result = localFileUtil.upload(file.getBytes(), file.getOriginalFilename(), type);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("文件上传校验失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    /**
     * 兼容旧的头像上传接口（注册页登录前也可调用）
     */
    @Operation(summary = "上传头像")
    @PostMapping("/user/avatar")
    public Result<String> uploadAvatar(MultipartFile file) {
        try {
            UploadResult result = localFileUtil.upload(file.getBytes(), file.getOriginalFilename(), "avatar");
            return Result.success(result.getUrl());
        } catch (IllegalArgumentException e) {
            log.warn("头像上传校验失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败");
        }
    }

    /**
     * 头像允许免登录（注册页使用），其余类型必须携带有效 token
     */
    private boolean checkAuth(String type, String token, HttpServletResponse response) {
        if ("avatar".equals(type)) {
            return true;
        }
        try {
            JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}

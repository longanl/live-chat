package com.xuziran.livechat.controller;

import com.xuziran.livechat.result.Result;
import com.xuziran.livechat.utils.AliOssUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@Slf4j
@Tag(name = "文件上传相关接口")
public class FileController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("/uploadavatar687")
    public Result upload(MultipartFile file) throws Exception {
        log.info("上传文件{}",file.getOriginalFilename());
        String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件上传成功,url:{}",url);
        return Result.success(url);
    }
}

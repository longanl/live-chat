package com.xuziran.livechat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件上传结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 对外可访问的完整 URL */
    private String url;
    /** 原始文件名 */
    private String name;
    /** 文件大小（字节） */
    private Long size;
    /** 扩展名（不含点） */
    private String ext;
    /** 类型：avatar / image / video / file */
    private String type;
}

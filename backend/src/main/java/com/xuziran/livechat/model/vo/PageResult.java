package com.xuziran.livechat.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果（与 API 文档 1.7 约定一致）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private Integer page;
    private Integer size;
    private List<T> list;

    public static <T> PageResult<T> of(Long total, Integer page, Integer size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }
}
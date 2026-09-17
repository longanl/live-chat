package com.xuziran.livechat.common.exception;

/**
 * 业务异常：由全局异常处理器转换为 Result.error(msg)，供前端弹窗提示。
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
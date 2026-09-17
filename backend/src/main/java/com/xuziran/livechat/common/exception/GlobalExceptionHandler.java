package com.xuziran.livechat.common.exception;

import com.xuziran.livechat.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常：正常包装成 Result，HTTP 200 可以接受 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 静态资源 / 未知路径 404：
     * - 日志级别降到 WARN，不打全栈
     * - HTTP 状态码返回 404，让 Swagger UI / 前端能按状态码判断
     * - 不放进"系统繁忙"里
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(NoResourceFoundException e) {
        log.warn("静态资源不存在: {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error("资源不存在: " + e.getResourcePath()));
    }

    /**
     * 兜底：其余未捕获异常
     * - HTTP 500，不要再用 200
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e, HttpServletRequest request) {
        // springdoc / swagger 相关请求一旦进入兜底，直接按 404 处理，避免污染日志
        String uri = request.getRequestURI();
        if (uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/webjars")) {
            log.warn("Swagger 相关请求异常: {} -> {}", uri, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.error("Swagger 资源不存在"));
        }

        log.error("系统异常: {} {}", request.getMethod(), uri, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统繁忙，请稍后再试"));
    }
}
package com.xuziran.livechat.common.context;

/**
 * 用户上下文：由 JwtTokenInterceptor 在每次请求进入时写入当前登录用户ID，
 * Controller/Service 从本处取当前用户身份，不再信任前端传入的 userId 参数。
 *
 * 注意：必须保证请求结束时清理（afterCompletion），否则 Tomcat 线程池复用会串号。
 */
public class BaseContext {

    private static final ThreadLocal<Long> currentId = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        currentId.set(id);
    }

    public static Long getCurrentId() {
        return currentId.get();
    }

    public static void remove() {
        currentId.remove();
    }
}
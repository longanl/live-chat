package com.xuziran.livechat.common.interceptor;


import com.xuziran.livechat.common.context.BaseContext;
import com.xuziran.livechat.common.properties.JwtProperties;
import com.xuziran.livechat.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * jwt令牌校验的拦截器
 */
@AllArgsConstructor
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    /**
     * 校验jwt，并把用户ID写入 BaseContext 供本次请求使用
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //判断是不是登陆或注册方法
        if (request.getRequestURI().contains("/user/login") || request.getRequestURI().contains("/user/register")){
            log.info("当前请求是登陆或注册方法，不需要进行jwt校验");
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getTokenName());

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Object id = claims.get("id");
            if (!(id instanceof Number)) {
                throw new IllegalArgumentException("token缺少用户ID");
            }
            //3、通过，写入当前用户上下文
            BaseContext.setCurrentId(((Number) id).longValue());
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }

    /**
     * 请求结束清理线程局部变量，防止线程池复用串号
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }
}
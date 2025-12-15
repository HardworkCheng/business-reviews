package com.businessreviews.interceptor;

import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.context.UserContext;
import com.businessreviews.util.JwtUtil;
import com.businessreviews.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("=== AuthInterceptor ===");
        System.out.println("Request URI: " + requestURI);
        System.out.println("Method: " + method);
        
        // 笔记详情的 GET 请求不需要登录
        if ("GET".equals(method) && requestURI.matches(".*/notes/\\d+$")) {
            System.out.println("笔记详情 GET 请求，跳过认证");
            return true;
        }
        
        // 文件上传接口需要单独处理 Token，因为 multipart 请求不会自动带 Authorization header
        if (requestURI.contains("/upload/")) {
            System.out.println("文件上传接口，需要 Token 认证");
        }
        
        // 获取请求头中的token
        String authorization = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + (authorization != null ? authorization.substring(0, Math.min(30, authorization.length())) + "..." : "null"));
        
        // 检查token是否存在
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            System.out.println("Token 格式错误或不存在");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return false;
        }
        
        String token = authorization.substring(7);
        System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // 检查token是否在黑名单中
        String blacklistKey = RedisKeyConstants.TOKEN_BLACKLIST + token;
        if (redisUtil.hasKey(blacklistKey)) {
            System.out.println("Token 在黑名单中");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\",\"data\":null}");
            return false;
        }
        
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("Token 验证失败");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期，请重新登录\",\"data\":null}");
            return false;
        }
        
        // 解析用户ID并存入上下文
        Long userId = jwtUtil.getUserIdFromToken(token);
        System.out.println("User ID: " + userId);
        UserContext.setUserId(userId);
        System.out.println("UserContext 设置成功");
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }
}

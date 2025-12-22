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

/**
 * 移动端用户认证拦截器
 */
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
        
        // 定义可选认证的接口路径（不强制登录，但如果有token则解析用户ID）
        boolean isOptionalAuth = isOptionalAuthPath(requestURI, method);
        
        if (isOptionalAuth) {
            System.out.println("可选认证接口，不强制登录");
            // 尝试解析token获取用户ID（可选）
            String authorization = request.getHeader("Authorization");
            if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                try {
                    // 检查token是否在黑名单中
                    String blacklistKey = RedisKeyConstants.TOKEN_BLACKLIST + token;
                    if (!redisUtil.hasKey(blacklistKey) && jwtUtil.validateToken(token)) {
                        Long userId = jwtUtil.getUserIdFromToken(token);
                        UserContext.setUserId(userId);
                        System.out.println("可选认证 - 已登录用户ID: " + userId);
                    }
                } catch (Exception e) {
                    System.out.println("可选认证 - Token解析失败，以游客身份访问: " + e.getMessage());
                }
            } else {
                System.out.println("可选认证 - 游客访问");
            }
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
    
    /**
     * 判断是否为可选认证路径
     * 这些路径不强制登录，但如果有token则解析用户ID
     */
    private boolean isOptionalAuthPath(String requestURI, String method) {
        // 笔记详情 GET 请求
        if ("GET".equals(method) && requestURI.matches(".*/notes/\\d+$")) {
            return true;
        }
        
        // 商家相关 GET 请求（详情、评价列表、笔记列表）
        if ("GET".equals(method)) {
            // /shops/{id}, /shops/{id}/reviews, /shops/{id}/notes
            if (requestURI.matches(".*/shops/\\d+$") ||
                requestURI.matches(".*/shops/\\d+/reviews$") ||
                requestURI.matches(".*/shops/\\d+/notes$")) {
                return true;
            }
        }
        
        // 优惠券公开接口 - GET 请求
        if ("GET".equals(method)) {
            // /app/coupons, /app/coupons/available, /app/coupons/{id}
            if (requestURI.matches(".*/app/coupons(/available)?$") || 
                requestURI.matches(".*/app/coupons/\\d+$") ||
                requestURI.equals("/api/app/coupons") ||
                requestURI.equals("/api/app/coupons/available")) {
                return true;
            }
            
            // /coupons
            if (requestURI.equals("/api/coupons") || requestURI.equals("/coupons")) {
                return true;
            }
            
            // 秒杀公开接口
            if (requestURI.contains("/app/seckill/")) {
                return true;
            }
        }
        
        return false;
    }
}

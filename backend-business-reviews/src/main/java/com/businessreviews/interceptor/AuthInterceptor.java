package com.businessreviews.interceptor;

import com.businessreviews.common.Constants;
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
        // 获取请求头中的token
        String authorization = request.getHeader("Authorization");
        
        // 检查token是否存在
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return false;
        }
        
        String token = authorization.substring(7);
        
        // 检查token是否在黑名单中
        String blacklistKey = Constants.RedisKey.TOKEN_BLACKLIST + token;
        if (redisUtil.hasKey(blacklistKey)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\",\"data\":null}");
            return false;
        }
        
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期，请重新登录\",\"data\":null}");
            return false;
        }
        
        // 解析用户ID并存入上下文
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserContext.setUserId(userId);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }
}

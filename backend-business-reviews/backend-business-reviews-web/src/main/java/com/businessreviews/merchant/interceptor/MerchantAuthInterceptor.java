package com.businessreviews.merchant.interceptor;

import com.businessreviews.entity.MerchantUser;
import com.businessreviews.mapper.MerchantUserMapper;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 商家端认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final MerchantUserMapper merchantUserMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取Authorization头
        String authorization = request.getHeader("Authorization");
        
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            
            try {
                // 验证token
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    
                    // 查询用户信息
                    MerchantUser user = merchantUserMapper.selectById(userId);
                    if (user != null && user.getStatus() == 1) {
                        // 设置上下文
                        MerchantContext.setUserId(userId);
                        MerchantContext.setMerchantId(user.getMerchantId());
                        log.debug("商家用户认证成功: userId={}, merchantId={}", userId, user.getMerchantId());
                        return true;
                    }
                }
            } catch (Exception e) {
                log.warn("Token验证失败: {}", e.getMessage());
            }
        }
        
        // 未认证，返回401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\"}");
        } catch (Exception e) {
            log.error("写入响应失败", e);
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理上下文
        MerchantContext.clear();
    }
}

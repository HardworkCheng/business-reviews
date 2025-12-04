package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.request.FollowRequest;
import com.businessreviews.dto.request.UpdateUserInfoRequest;
import com.businessreviews.dto.request.ChangePasswordRequest;
import com.businessreviews.dto.request.ChangePhoneRequest;
import com.businessreviews.dto.response.*;
import com.businessreviews.service.UserService;
import com.businessreviews.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest httpRequest) {
        // 从 UserContext 获取
        Long userId = UserContext.getUserId();
        log.info("=== getUserInfo ===");
        log.info("UserContext.getUserId(): {}", userId);
        
        // 如果 UserContext 中没有，直接从请求头获取
        if (userId == null) {
            String authorization = httpRequest.getHeader("Authorization");
            log.info("Authorization header: {}", authorization != null ? authorization.substring(0, Math.min(30, authorization.length())) + "..." : "null");
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                
                try {
                    userId = jwtUtil.getUserIdFromToken(token);
                    log.info("从 Token 解析到的 userId: {}", userId);
                    UserContext.setUserId(userId);
                } catch (Exception e) {
                    log.error("Token 解析失败", e);
                    return Result.error(401, "登录已过期，请重新登录");
                }
            } else {
                log.warn("没有找到 Authorization 请求头");
                return Result.error(401, "未登录");
            }
        }
        
        log.info("最终查询的 userId: {}", userId);
        UserInfoResponse response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody @Valid UpdateUserInfoRequest request, HttpServletRequest httpRequest) {
        System.out.println("=== updateUserInfo ===");
        
        // 从 UserContext 获取
        Long userId = UserContext.getUserId();
        System.out.println("User ID from UserContext: " + userId);
        
        // 如果 UserContext 中没有，直接从请求头获取
        if (userId == null) {
            System.out.println("UserContext 中没有 userId，尝试从请求头获取");
            String authorization = httpRequest.getHeader("Authorization");
            System.out.println("Authorization header: " + authorization);
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");
                
                try {
                    userId = jwtUtil.getUserIdFromToken(token);
                    System.out.println("Parsed userId from token: " + userId);
                    UserContext.setUserId(userId);
                } catch (Exception e) {
                    System.err.println("Token 解析失败: " + e.getMessage());
                    return Result.error(401, "登录已过期，请重新登录");
                }
            } else {
                System.err.println("没有找到 Authorization 请求头");
                return Result.error(401, "未登录");
            }
        }
        
        System.out.println("Final userId: " + userId);
        System.out.println("Update data: " + request);
        
        userService.updateUserInfo(userId, request);
        return Result.success("更新成功");
    }

    /**
     * 获取用户公开主页信息
     */
    @GetMapping("/profile/{userId}")
    public Result<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        Long currentUserId = UserContext.getUserId();
        UserProfileResponse response = userService.getUserProfile(userId, currentUserId);
        return Result.success(response);
    }

    /**
     * 获取我的笔记列表
     */
    @GetMapping("/notes")
    public Result<PageResult<NoteItemResponse>> getMyNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemResponse> result = userService.getMyNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/favorites")
    public Result<PageResult<FavoriteItemResponse>> getMyFavorites(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<FavoriteItemResponse> result = userService.getMyFavorites(userId, type, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取浏览历史
     */
    @GetMapping("/history")
    public Result<PageResult<HistoryItemResponse>> getBrowseHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<HistoryItemResponse> result = userService.getBrowseHistory(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    public Result<?> followUser(@RequestBody @Valid FollowRequest request) {
        Long userId = UserContext.requireUserId();
        userService.followUser(userId, Long.parseLong(request.getUserId()));
        return Result.success("关注成功");
    }

    /**
     * 取消关注用户
     */
    @DeleteMapping("/follow/{targetUserId}")
    public Result<?> unfollowUser(@PathVariable Long targetUserId) {
        Long userId = UserContext.requireUserId();
        userService.unfollowUser(userId, targetUserId);
        return Result.success("取消关注成功");
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/following")
    public Result<PageResult<UserItemResponse>> getFollowingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<UserItemResponse> result = userService.getFollowingList(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/followers")
    public Result<PageResult<UserItemResponse>> getFollowerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<UserItemResponse> result = userService.getFollowerList(userId, pageNum, pageSize);
        return Result.success(result);
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, HttpServletRequest httpRequest) {
        // 从 UserContext 获取
        Long userId = UserContext.getUserId();
        
        // 如果 UserContext 中没有，直接从请求头获取
        if (userId == null) {
            String authorization = httpRequest.getHeader("Authorization");
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                
                try {
                    userId = jwtUtil.getUserIdFromToken(token);
                    UserContext.setUserId(userId);
                } catch (Exception e) {
                    return Result.error(401, "登录已过期，请重新登录");
                }
            } else {
                return Result.error(401, "未登录");
            }
        }
        
        userService.changePassword(userId, request);
        return Result.success("密码修改成功");
    }
    
    /**
     * 修改手机号
     */
    @PutMapping("/phone")
    public Result<?> changePhone(@RequestBody @Valid ChangePhoneRequest request, HttpServletRequest httpRequest) {
        // 从 UserContext 获取
        Long userId = UserContext.getUserId();
        
        // 如果 UserContext 中没有，直接从请求头获取
        if (userId == null) {
            String authorization = httpRequest.getHeader("Authorization");
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                
                try {
                    userId = jwtUtil.getUserIdFromToken(token);
                    UserContext.setUserId(userId);
                } catch (Exception e) {
                    return Result.error(401, "登录已过期，请重新登录");
                }
            } else {
                return Result.error(401, "未登录");
            }
        }
        
        userService.changePhone(userId, request);
        return Result.success("手机号修改成功");
    }
    
    /**
     * 获取用户当前绑定的手机号
     */
    @GetMapping("/phone")
    public Result<String> getUserPhone(HttpServletRequest httpRequest) {
        // 从 UserContext 获取
        Long userId = UserContext.getUserId();
        
        // 如果 UserContext 中没有，直接从请求头获取
        if (userId == null) {
            String authorization = httpRequest.getHeader("Authorization");
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                
                try {
                    userId = jwtUtil.getUserIdFromToken(token);
                    UserContext.setUserId(userId);
                } catch (Exception e) {
                    return Result.error(401, "登录已过期，请重新登录");
                }
            } else {
                return Result.error(401, "未登录");
            }
        }
        
        String phone = userService.getUserPhone(userId);
        return Result.success(phone);
    }
}

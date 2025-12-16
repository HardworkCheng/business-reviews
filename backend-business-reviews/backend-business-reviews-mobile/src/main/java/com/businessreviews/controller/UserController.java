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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端用户控制器 (UniApp)
 * 
 * 提供移动端用户的个人信息管理API：
 * - GET /user/info - 获取当前用户信息
 * - PUT /user/info - 更新用户信息
 * - GET /user/profile/{userId} - 获取用户公开主页
 * - GET /user/notes - 获取我的笔记列表
 * - GET /user/favorites - 获取我的收藏列表
 * - GET /user/history - 获取浏览历史
 * - POST /user/follow - 关注用户
 * - DELETE /user/follow/{targetUserId} - 取消关注
 * - GET /user/following - 获取关注列表
 * - GET /user/followers - 获取粉丝列表
 * - PUT /user/password - 修改密码
 * - PUT /user/phone - 修改手机号
 * 
 * @see com.businessreviews.service.UserService
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        Long userId = UserContext.requireUserId();
        log.info("查询用户信息: userId={}", userId);
        UserInfoResponse response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody @Valid UpdateUserInfoRequest request) {
        Long userId = UserContext.requireUserId();
        log.info("更新用户信息: userId={}", userId);
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
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Long userId = UserContext.requireUserId();
        userService.changePassword(userId, request);
        return Result.success("密码修改成功");
    }
    
    /**
     * 修改手机号
     */
    @PutMapping("/phone")
    public Result<?> changePhone(@RequestBody @Valid ChangePhoneRequest request) {
        Long userId = UserContext.requireUserId();
        userService.changePhone(userId, request);
        return Result.success("手机号修改成功");
    }
    
    /**
     * 获取用户当前绑定的手机号
     */
    @GetMapping("/phone")
    public Result<String> getUserPhone() {
        Long userId = UserContext.requireUserId();
        String phone = userService.getUserPhone(userId);
        return Result.success(phone);
    }
}

package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.request.FollowRequest;
import com.businessreviews.dto.request.UpdateUserInfoRequest;
import com.businessreviews.dto.response.*;
import com.businessreviews.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        UserInfoResponse response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody @Valid UpdateUserInfoRequest request) {
        Long userId = UserContext.requireUserId();
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
}

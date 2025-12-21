package com.businessreviews.web.app;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.model.dto.app.FollowDTO;
import com.businessreviews.model.dto.app.UpdateUserInfoDTO;
import com.businessreviews.model.dto.app.ChangePasswordDTO;
import com.businessreviews.model.dto.app.ChangePhoneDTO;
import com.businessreviews.model.vo.*;
import com.businessreviews.service.app.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端用户控制器 (UniApp)
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = UserContext.requireUserId();
        log.info("查询用户信息: userId={}", userId);
        UserInfoVO response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody @Valid UpdateUserInfoDTO request) {
        Long userId = UserContext.requireUserId();
        log.info("更新用户信息: userId={}", userId);
        userService.updateUserInfo(userId, request);
        return Result.success("更新成功");
    }

    @GetMapping("/profile/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        Long currentUserId = UserContext.getUserId();
        UserProfileVO response = userService.getUserProfile(userId, currentUserId);
        return Result.success(response);
    }


    @GetMapping("/notes")
    public Result<PageResult<NoteItemVO>> getMyNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemVO> result = userService.getMyNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/favorites")
    public Result<PageResult<FavoriteItemVO>> getMyFavorites(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<FavoriteItemVO> result = userService.getMyFavorites(userId, type, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/history")
    public Result<PageResult<HistoryItemVO>> getBrowseHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<HistoryItemVO> result = userService.getBrowseHistory(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/follow")
    public Result<?> followUser(@RequestBody @Valid FollowDTO request) {
        Long userId = UserContext.requireUserId();
        userService.followUser(userId, Long.parseLong(request.getUserId()));
        return Result.success("关注成功");
    }

    @DeleteMapping("/follow/{targetUserId}")
    public Result<?> unfollowUser(@PathVariable Long targetUserId) {
        Long userId = UserContext.requireUserId();
        userService.unfollowUser(userId, targetUserId);
        return Result.success("取消关注成功");
    }

    @GetMapping("/following")
    public Result<PageResult<UserItemVO>> getFollowingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<UserItemVO> result = userService.getFollowingList(userId, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/followers")
    public Result<PageResult<UserItemVO>> getFollowerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<UserItemVO> result = userService.getFollowerList(userId, pageNum, pageSize);
        return Result.success(result);
    }
    
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordDTO request) {
        Long userId = UserContext.requireUserId();
        userService.changePassword(userId, request);
        return Result.success("密码修改成功");
    }
    
    @PutMapping("/phone")
    public Result<?> changePhone(@RequestBody @Valid ChangePhoneDTO request) {
        Long userId = UserContext.requireUserId();
        userService.changePhone(userId, request);
        return Result.success("手机号修改成功");
    }
    
    @GetMapping("/phone")
    public Result<String> getUserPhone() {
        Long userId = UserContext.requireUserId();
        String phone = userService.getUserPhone(userId);
        return Result.success(phone);
    }
}

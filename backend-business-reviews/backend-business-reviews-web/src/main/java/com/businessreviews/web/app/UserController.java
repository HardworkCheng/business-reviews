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
 * <p>
 * 提供移动端用户的个人信息管理、关注列表、浏览历史等API。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        Long userId = UserContext.requireUserId();
        log.info("查询用户信息: userId={}", userId);
        UserInfoVO response = userService.getUserInfo(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     *
     * @param request 更新请求参数
     * @return 成功结果
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody @Valid UpdateUserInfoDTO request) {
        Long userId = UserContext.requireUserId();
        log.info("更新用户信息: userId={}", userId);
        userService.updateUserInfo(userId, request);
        return Result.success("更新成功");
    }

    /**
     * 获取用户个人主页信息
     *
     * @param userId 目标用户ID
     * @return 个人主页信息
     */
    @GetMapping("/profile/{userId}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        Long currentUserId = UserContext.getUserId();
        UserProfileVO response = userService.getUserProfile(userId, currentUserId);
        return Result.success(response);
    }

    /**
     * 获取我的笔记列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记列表
     */
    @GetMapping("/notes")
    public Result<PageResult<NoteItemVO>> getMyNotes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<NoteItemVO> result = userService.getMyNotes(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取我的收藏列表
     *
     * @param type     收藏类型
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    @GetMapping("/favorites")
    public Result<PageResult<FavoriteItemVO>> getMyFavorites(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<FavoriteItemVO> result = userService.getMyFavorites(userId, type, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取浏览历史
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 浏览历史列表
     */
    @GetMapping("/history")
    public Result<PageResult<HistoryItemVO>> getBrowseHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<HistoryItemVO> result = userService.getBrowseHistory(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 关注用户
     *
     * @param request 关注请求
     * @return 成功结果
     */
    @PostMapping("/follow")
    public Result<?> followUser(@RequestBody @Valid FollowDTO request) {
        Long userId = UserContext.requireUserId();
        userService.followUser(userId, Long.parseLong(request.getUserId()));
        return Result.success("关注成功");
    }

    /**
     * 取消关注用户
     *
     * @param targetUserId 目标用户ID
     * @return 成功结果
     */
    @DeleteMapping("/follow/{targetUserId}")
    public Result<?> unfollowUser(@PathVariable Long targetUserId) {
        Long userId = UserContext.requireUserId();
        userService.unfollowUser(userId, targetUserId);
        return Result.success("取消关注成功");
    }

    /**
     * 获取关注列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    @GetMapping("/following")
    public Result<PageResult<UserItemVO>> getFollowingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<UserItemVO> result = userService.getFollowingList(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 获取粉丝列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
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

package com.businessreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.FollowRequest;
import com.businessreviews.dto.request.UpdateUserInfoRequest;
import com.businessreviews.dto.request.ChangePasswordRequest;
import com.businessreviews.dto.response.*;
import com.businessreviews.entity.User;

public interface UserService extends IService<User> {
    
    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);
    
    /**
     * 注册新用户
     */
    User register(String phone);
    
    /**
     * 获取当前用户信息
     */
    UserInfoResponse getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UpdateUserInfoRequest request);
    
    /**
     * 获取用户公开主页信息
     */
    UserProfileResponse getUserProfile(Long userId, Long currentUserId);
    
    /**
     * 获取我的笔记列表
     */
    PageResult<NoteItemResponse> getMyNotes(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取我的收藏列表
     */
    PageResult<FavoriteItemResponse> getMyFavorites(Long userId, Integer type, Integer pageNum, Integer pageSize);
    
    /**
     * 获取浏览历史
     */
    PageResult<HistoryItemResponse> getBrowseHistory(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 关注用户
     */
    void followUser(Long userId, Long targetUserId);
    
    /**
     * 取消关注用户
     */
    void unfollowUser(Long userId, Long targetUserId);
    
    /**
     * 获取关注列表
     */
    PageResult<UserItemResponse> getFollowingList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取粉丝列表
     */
    PageResult<UserItemResponse> getFollowerList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long userId, Long targetUserId);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}

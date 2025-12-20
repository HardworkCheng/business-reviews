package com.businessreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.ChangePhoneDTO;
import com.businessreviews.model.dto.FollowDTO;
import com.businessreviews.model.dto.UpdateUserInfoDTO;
import com.businessreviews.model.dto.ChangePasswordDTO;
import com.businessreviews.model.vo.*;
import com.businessreviews.model.dataobject.UserDO;

public interface UserService extends IService<UserDO> {
    
    /**
     * 根据手机号查询用户
     */
    UserDO getByPhone(String phone);
    
    /**
     * 注册新用户
     */
    UserDO register(String phone);
    
    /**
     * 获取当前用户信息
     */
    UserInfoVO getUserInfo(Long userId);
    
    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UpdateUserInfoDTO request);
    
    /**
     * 获取用户公开主页信息
     */
    UserProfileVO getUserProfile(Long userId, Long currentUserId);
    
    /**
     * 获取我的笔记列表
     */
    PageResult<NoteItemVO> getMyNotes(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取我的收藏列表
     */
    PageResult<FavoriteItemVO> getMyFavorites(Long userId, Integer type, Integer pageNum, Integer pageSize);
    
    /**
     * 获取浏览历史
     */
    PageResult<HistoryItemVO> getBrowseHistory(Long userId, Integer pageNum, Integer pageSize);
    
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
    PageResult<UserItemVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取粉丝列表
     */
    PageResult<UserItemVO> getFollowerList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long userId, Long targetUserId);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordDTO request);
    
    /**
     * 修改手机号
     */
    void changePhone(Long userId, ChangePhoneDTO request);
    
    /**
     * 获取用户当前绑定的手机号
     */
    String getUserPhone(Long userId);
}

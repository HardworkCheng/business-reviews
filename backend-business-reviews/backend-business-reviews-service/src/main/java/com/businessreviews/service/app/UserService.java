package com.businessreviews.service.app;

import com.baomidou.mybatisplus.extension.service.IService;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.app.ChangePhoneDTO;
import com.businessreviews.model.dto.app.FollowDTO;
import com.businessreviews.model.dto.app.UpdateUserInfoDTO;
import com.businessreviews.model.dto.app.ChangePasswordDTO;
import com.businessreviews.model.vo.*;
import com.businessreviews.model.dataobject.UserDO;

/**
 * 用户端用户服务接口
 * <p>
 * 提供UniApp移动端的用户信息管理、关注、收藏等功能
 * </p>
 * 
 * @author businessreviews
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户DO对象
     */
    UserDO getByPhone(String phone);

    /**
     * 注册新用户
     * 
     * @param phone 手机号
     * @return 注册后的用户DO对象
     */
    UserDO register(String phone);

    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID
     * @return 个人信息VO
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     * 
     * @param userId  用户ID
     * @param request 更新请求对象
     */
    void updateUserInfo(Long userId, UpdateUserInfoDTO request);

    /**
     * 获取用户公开主页信息
     * 
     * @param userId        目标用户ID(要查看的主页)
     * @param currentUserId 当前登录用户ID（用于判断关注状态）
     * @return 用户公开资料VO
     */
    UserProfileVO getUserProfile(Long userId, Long currentUserId);

    /**
     * 获取我的笔记列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 我的笔记列表分页数据
     */
    PageResult<NoteItemVO> getMyNotes(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取我的收藏列表
     * 
     * @param userId   用户ID
     * @param type     收藏类型（1笔记，2商家）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 收藏项列表分页数据
     */
    PageResult<FavoriteItemVO> getMyFavorites(Long userId, Integer type, Integer pageNum, Integer pageSize);

    /**
     * 获取浏览历史
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 浏览历史列表分页数据
     */
    PageResult<HistoryItemVO> getBrowseHistory(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 关注用户
     * 
     * @param userId       关注者ID
     * @param targetUserId 被关注者ID
     */
    void followUser(Long userId, Long targetUserId);

    /**
     * 取消关注用户
     * 
     * @param userId       操作者ID
     * @param targetUserId 目标用户ID
     */
    void unfollowUser(Long userId, Long targetUserId);

    /**
     * 获取关注列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 关注列表分页数据
     */
    PageResult<UserItemVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取粉丝列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 粉丝列表分页数据
     */
    PageResult<UserItemVO> getFollowerList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 检查是否已关注
     * 
     * @param userId       关注者ID
     * @param targetUserId 被关注者ID
     * @return true=已关注，false=未关注
     */
    boolean isFollowing(Long userId, Long targetUserId);

    /**
     * 修改密码
     * 
     * @param userId  用户ID
     * @param request 修改密码请求对象
     */
    void changePassword(Long userId, ChangePasswordDTO request);

    /**
     * 修改手机号
     * 
     * @param userId  用户ID
     * @param request 修改手机号请求对象
     */
    void changePhone(Long userId, ChangePhoneDTO request);

    /**
     * 获取用户当前绑定的手机号
     * 
     * @param userId 用户ID
     * @return 手机号
     */
    String getUserPhone(Long userId);
}

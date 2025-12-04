package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.Constants;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.UpdateUserInfoRequest;
import com.businessreviews.dto.response.*;
import com.businessreviews.entity.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.MessageService;
import com.businessreviews.service.UserService;
import com.businessreviews.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;
    private final UserFollowMapper userFollowMapper;
    private final NoteMapper noteMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final BrowseHistoryMapper browseHistoryMapper;
    private final ShopMapper shopMapper;
    private final RedisUtil redisUtil;
    private final MessageService messageService;

    @Override
    public User getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setUsername("用户" + phone.substring(7));
        user.setAvatar("https://example.com/default-avatar.png");
        user.setStatus(1);
        userMapper.insert(user);
        
        UserStats stats = new UserStats();
        stats.setUserId(user.getId());
        stats.setFollowingCount(0);
        stats.setFollowerCount(0);
        stats.setNoteCount(0);
        stats.setLikeCount(0);
        stats.setFavoriteCount(0);
        userStatsMapper.insert(stats);
        
        return user;
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        // 尝试从缓存获取
        String cacheKey = Constants.RedisKey.USER_INFO + userId;
        UserInfoResponse cached = redisUtil.getObject(cacheKey, UserInfoResponse.class);
        if (cached != null) {
            return cached;
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        UserStats stats = userStatsMapper.selectByUserId(userId);
        
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        response.setPhone(maskPhone(user.getPhone()));
        response.setGender(user.getGender());
        response.setBirthday(user.getBirthday());
        
        if (stats != null) {
            response.setFollowingCount(stats.getFollowingCount());
            response.setFollowerCount(stats.getFollowerCount());
            response.setLikeCount(stats.getLikeCount());
            response.setFavoriteCount(stats.getFavoriteCount());
            response.setNoteCount(stats.getNoteCount());
        }
        
        // 缓存30分钟
        redisUtil.setObject(cacheKey, response, 1800);
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        
        userMapper.updateById(user);
        
        // 清除缓存
        redisUtil.delete(Constants.RedisKey.USER_INFO + userId);
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        UserStats stats = userStatsMapper.selectByUserId(userId);
        
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        
        if (stats != null) {
            response.setFollowingCount(stats.getFollowingCount());
            response.setFollowerCount(stats.getFollowerCount());
            response.setLikeCount(stats.getLikeCount());
            response.setNoteCount(stats.getNoteCount());
        }
        
        // 检查是否已关注
        if (currentUserId != null && !currentUserId.equals(userId)) {
            response.setIsFollowing(isFollowing(currentUserId, userId));
        } else {
            response.setIsFollowing(false);
        }
        
        return response;
    }

    @Override
    public PageResult<NoteItemResponse> getMyNotes(Long userId, Integer pageNum, Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(Note::getStatus, 1)
               .orderByDesc(Note::getCreatedAt);
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<FavoriteItemResponse> getMyFavorites(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        Page<UserFavorite> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(type != null, UserFavorite::getType, type)
               .orderByDesc(UserFavorite::getCreatedAt);
        
        Page<UserFavorite> favPage = userFavoriteMapper.selectPage(page, wrapper);
        
        List<FavoriteItemResponse> list = new ArrayList<>();
        for (UserFavorite fav : favPage.getRecords()) {
            FavoriteItemResponse item = new FavoriteItemResponse();
            item.setId(fav.getId().toString());
            item.setType(fav.getType());
            item.setTargetId(fav.getTargetId().toString());
            item.setCreatedAt(fav.getCreatedAt().toString());
            
            if (fav.getType() == 1) {
                // 笔记
                Note note = noteMapper.selectById(fav.getTargetId());
                if (note != null) {
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                }
            } else if (fav.getType() == 2) {
                // 商家
                Shop shop = shopMapper.selectById(fav.getTargetId());
                if (shop != null) {
                    item.setImage(shop.getHeaderImage());
                    item.setTitle(shop.getName());
                }
            }
            list.add(item);
        }
        
        return PageResult.of(list, favPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<HistoryItemResponse> getBrowseHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<BrowseHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId)
               .orderByDesc(BrowseHistory::getCreatedAt);
        
        Page<BrowseHistory> historyPage = browseHistoryMapper.selectPage(page, wrapper);
        
        List<HistoryItemResponse> list = new ArrayList<>();
        for (BrowseHistory history : historyPage.getRecords()) {
            HistoryItemResponse item = new HistoryItemResponse();
            item.setId(history.getId().toString());
            item.setType(history.getType());
            item.setTargetId(history.getTargetId().toString());
            item.setCreatedAt(history.getCreatedAt().toString());
            
            if (history.getType() == 1) {
                Note note = noteMapper.selectById(history.getTargetId());
                if (note != null) {
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                }
            } else if (history.getType() == 2) {
                Shop shop = shopMapper.selectById(history.getTargetId());
                if (shop != null) {
                    item.setImage(shop.getHeaderImage());
                    item.setTitle(shop.getName());
                }
            }
            list.add(item);
        }
        
        return PageResult.of(list, historyPage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(40001, "不能关注自己");
        }
        
        // 检查目标用户是否存在
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        // 检查是否已关注
        if (isFollowing(userId, targetUserId)) {
            throw new BusinessException(40001, "已关注该用户");
        }
        
        // 插入关注记录
        UserFollow follow = new UserFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(targetUserId);
        userFollowMapper.insert(follow);
        
        // 更新统计数据
        userStatsMapper.incrementFollowingCount(userId);
        userStatsMapper.incrementFollowerCount(targetUserId);
        
        // 发送关注通知
        User user = userMapper.selectById(userId);
        messageService.sendNotification(targetUserId, "新粉丝", 
                user.getUsername() + " 关注了你", 3, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long userId, Long targetUserId) {
        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowUserId, targetUserId);
        
        UserFollow follow = userFollowMapper.selectOne(wrapper);
        if (follow == null) {
            throw new BusinessException(40001, "未关注该用户");
        }
        
        // 删除关注记录
        userFollowMapper.deleteById(follow.getId());
        
        // 更新统计数据
        userStatsMapper.decrementFollowingCount(userId);
        userStatsMapper.decrementFollowerCount(targetUserId);
    }

    @Override
    public PageResult<UserItemResponse> getFollowingList(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .orderByDesc(UserFollow::getCreatedAt);
        
        Page<UserFollow> followPage = userFollowMapper.selectPage(page, wrapper);
        
        List<UserItemResponse> list = followPage.getRecords().stream()
                .map(f -> {
                    User user = userMapper.selectById(f.getFollowUserId());
                    return convertToUserItem(user);
                })
                .collect(Collectors.toList());
        
        return PageResult.of(list, followPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<UserItemResponse> getFollowerList(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserFollow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowUserId, userId)
               .orderByDesc(UserFollow::getCreatedAt);
        
        Page<UserFollow> followPage = userFollowMapper.selectPage(page, wrapper);
        
        List<UserItemResponse> list = followPage.getRecords().stream()
                .map(f -> {
                    User user = userMapper.selectById(f.getUserId());
                    return convertToUserItem(user);
                })
                .collect(Collectors.toList());
        
        return PageResult.of(list, followPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowUserId, targetUserId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    private NoteItemResponse convertToNoteItem(Note note) {
        NoteItemResponse item = new NoteItemResponse();
        item.setId(note.getId().toString());
        item.setImage(note.getCoverImage());
        item.setTitle(note.getTitle());
        item.setLikes(note.getLikeCount());
        item.setViews(note.getViewCount());
        item.setCreatedAt(note.getCreatedAt().toString());
        return item;
    }

    private UserItemResponse convertToUserItem(User user) {
        if (user == null) {
            return null;
        }
        UserItemResponse item = new UserItemResponse();
        item.setUserId(user.getId().toString());
        item.setUsername(user.getUsername());
        item.setAvatar(user.getAvatar());
        item.setBio(user.getBio());
        return item;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.constants.SmsCodeConstants;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.ChangePhoneRequest;
import com.businessreviews.dto.request.UpdateUserInfoRequest;
import com.businessreviews.dto.request.ChangePasswordRequest;
import com.businessreviews.dto.response.*;
import com.businessreviews.entity.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.MessageService;
import com.businessreviews.service.UserService;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    
    /**
     * 默认头像列表 - 从阿里云OSS上随机选取
     */
    private static final String[] DEFAULT_AVATARS = {
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head2.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head3.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head4.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head5.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head6.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head8.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head9.png",
        "https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head10.png"
    };
    
    private static final Random RANDOM = new Random();

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
        
        // 随机选择一个默认头像
        user.setAvatar(getRandomAvatar());
        
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
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}, 可能是Token已失效或用户已被删除", userId);
            // 清除可能的脏缓存
            redisUtil.delete(RedisKeyConstants.USER_INFO + userId);
            throw new BusinessException(40401, "用户信息已失效,请重新登录");
        }
        
        UserStats stats = userStatsMapper.selectByUserId(userId);
        
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        response.setPhone(maskPhone(user.getPhone())); // 脱敏后的手机号
        response.setFullPhone(user.getPhone()); // 完整手机号
        response.setGender(user.getGender());
        response.setBirthday(user.getBirthday());
        response.setWechatOpenid(user.getWechatOpenid());
        response.setQqOpenid(user.getQqOpenid());
        response.setWeiboUid(user.getWeiboUid());
        
        if (stats != null) {
            response.setFollowingCount(stats.getFollowingCount());
            response.setFollowerCount(stats.getFollowerCount());
            response.setLikeCount(stats.getLikeCount());
            response.setFavoriteCount(stats.getFavoriteCount());
            response.setNoteCount(stats.getNoteCount());
        }
        
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
        if (request.getWechatOpenid() != null) {
            user.setWechatOpenid(request.getWechatOpenid());
        }
        if (request.getQqOpenid() != null) {
            user.setQqOpenid(request.getQqOpenid());
        }
        if (request.getWeiboUid() != null) {
            user.setWeiboUid(request.getWeiboUid());
        }
        
        userMapper.updateById(user);
        
        // 清除缓存
        redisUtil.delete(RedisKeyConstants.USER_INFO + userId);
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
                    item.setLikes(note.getLikeCount());
                }
            } else if (fav.getType() == 2) {
                // 商家
                Shop shop = shopMapper.selectById(fav.getTargetId());
                if (shop != null) {
                    item.setImage(shop.getHeaderImage());
                    item.setTitle(shop.getName());
                    item.setLikes(0); // 商家没有点赞数
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
            item.setViewTime(TimeUtil.formatRelativeTime(history.getCreatedAt()));
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
        // 使用新的系统通知方法，包含发送者信息
        messageService.sendSystemNotice(targetUserId, userId, 3, userId,
                user.getUsername() + " 关注了你", user.getAvatar());
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
                    UserItemResponse item = convertToUserItem(user);
                    // 关注列表中的所有用户都是已关注的
                    if (item != null) {
                        item.setIsFollowing(true);
                    }
                    return item;
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
                    UserItemResponse item = convertToUserItem(user);
                    // 设置当前用户是否关注了该粉丝
                    if (item != null) {
                        item.setIsFollowing(isFollowing(userId, user.getId()));
                    }
                    return item;
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        // 验证手机号
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            throw new BusinessException(40001, "请先绑定手机号");
        }
        
        // 验证验证码
        String codeKey = RedisKeyConstants.SMS_CODE + user.getPhone();
        String cachedCode = redisUtil.get(codeKey);
        if (cachedCode == null || !cachedCode.equals(request.getCode())) {
            throw new BusinessException(40002, "验证码错误或已过期");
        }
        
        // 验证旧密码
        if (!request.getOldPassword().equals(user.getPassword())) {
            throw new BusinessException(40003, "旧密码错误");
        }
        
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(40001, "两次密码输入不一致");
        }
        
        // 验证密码长度
        if (request.getNewPassword().length() < 6 || request.getNewPassword().length() > 20) {
            throw new BusinessException(40001, "密码长度必须在6-20位之间");
        }
        
        // 更新密码
        user.setPassword(request.getNewPassword());
        userMapper.updateById(user);
        
        // 删除验证码
        redisUtil.delete(codeKey);
        
        // 清除缓存
        redisUtil.delete(RedisKeyConstants.USER_INFO + userId);
        
        log.info("用户{}:密码修改成功", userId);
    }
    
    /**
     * 随机获取一个默认头像 URL
     *
     * @return 头像 URL
     */
    private String getRandomAvatar() {
        int index = RANDOM.nextInt(DEFAULT_AVATARS.length);
        String avatar = DEFAULT_AVATARS[index];
        log.info("为新用户随机分配头像: {}", avatar);
        return avatar;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePhone(Long userId, ChangePhoneRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        
        String oldPhone = user.getPhone();
        String newPhone = request.getNewPhone();
        
        // 检查是否绑定了手机号
        if (oldPhone == null || oldPhone.isEmpty()) {
            throw new BusinessException(40001, "当前账号未绑定手机号");
        }
        
        // 检查新手机号是否与旧手机号相同
        if (oldPhone.equals(newPhone)) {
            throw new BusinessException(40001, "新手机号不能与当前手机号相同");
        }
        
        // 检查修改频率限制(24小时内最多修改一次)
        String limitKey = RedisKeyConstants.CHANGE_PHONE_LIMIT + userId;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40005, "24小时内只能修改一次手机号");
        }
        
        // 检查新手机号是否已被其他用户绑定
        User existingUser = userMapper.selectByPhone(newPhone);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException(40001, "该手机号已被其他账号绑定");
        }
        
        // 验证原手机号验证码
        String oldCodeKey = RedisKeyConstants.SMS_CODE + oldPhone;
        String cachedOldCode = redisUtil.get(oldCodeKey);
        if (cachedOldCode == null || !cachedOldCode.equals(request.getOldPhoneCode())) {
            throw new BusinessException(40002, "原手机号验证码错误或已过期");
        }
        
        // 验证新手机号验证码
        String newCodeKey = RedisKeyConstants.SMS_CODE + newPhone;
        String cachedNewCode = redisUtil.get(newCodeKey);
        if (cachedNewCode == null || !cachedNewCode.equals(request.getNewPhoneCode())) {
            throw new BusinessException(40002, "新手机号验证码错误或已过期");
        }
        
        // 更新手机号
        user.setPhone(newPhone);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 删除验证码
        redisUtil.delete(oldCodeKey);
        redisUtil.delete(newCodeKey);
        
        // 设置修改频率限制(24小时)
        redisUtil.set(limitKey, "1", 86400);
        
        // 清除用户信息缓存
        redisUtil.delete(RedisKeyConstants.USER_INFO + userId);
        
        log.info("用户{}手机号修改成功: {} -> {}", userId, maskPhone(oldPhone), maskPhone(newPhone));
    }
    
    @Override
    public String getUserPhone(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        return user.getPhone();
    }
}

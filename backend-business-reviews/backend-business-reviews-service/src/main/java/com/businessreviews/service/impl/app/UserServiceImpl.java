package com.businessreviews.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.constants.SmsCodeConstants;
import com.businessreviews.common.DefaultAvatar;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.app.ChangePhoneDTO;
import com.businessreviews.model.dto.app.UpdateUserInfoDTO;
import com.businessreviews.model.dto.app.ChangePasswordDTO;
import com.businessreviews.model.vo.*;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.app.MessageService;
import com.businessreviews.enums.UserStatus;
import com.businessreviews.service.app.UserService;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * <p>
 * 处理C端用户的个人信息管理、社交关系及互动历史。
 * 核心功能包括：
 * 1. 用户信息查询与修改（改密、换绑手机）
 * 2. 用户个人主页数据聚合
 * 3. 关注与粉丝管理
 * 4. 用户历史记录查询（笔记、收藏、浏览历史）
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

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
    public UserDO getByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    /**
     * 注册新用户
     * <p>
     * 创建用户基础信息和统计信息记录。
     * 用户名默认为"用户"+手机号后四位，头像随机分配。
     * </p>
     *
     * @param phone 手机号
     * @return 注册成功的用户实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDO register(String phone) {
        UserDO user = new UserDO();
        user.setPhone(phone);
        user.setUsername("用户" + phone.substring(7));

        // 使用DefaultAvatar常量类随机选择一个默认头像
        user.setAvatar(DefaultAvatar.getRandomAvatar());

        user.setStatus(UserStatus.NORMAL.getCode());
        userMapper.insert(user);

        UserStatsDO stats = new UserStatsDO();
        stats.setUserId(user.getId());
        stats.setFollowingCount(0);
        stats.setFollowerCount(0);
        stats.setNoteCount(0);
        stats.setLikeCount(0);
        stats.setFavoriteCount(0);
        userStatsMapper.insert(stats);

        return user;
    }

    /**
     * 获取当前登录用户个人信息
     * <p>
     * 返回用户详细资料及各项统计数据（关注、粉丝、获赞等）。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户个人信息VO
     * @throws BusinessException 如果用户不存在(40401)
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}, 可能是Token已失效或用户已被删除", userId);
            // 清除可能的脏缓存
            redisUtil.delete(RedisKeyConstants.USER_INFO + userId);
            throw new BusinessException(40401, "用户信息已失效,请重新登录");
        }

        UserStatsDO stats = userStatsMapper.selectByUserId(userId);

        UserInfoVO response = new UserInfoVO();
        response.setUserId(user.getId().toString());
        response.setUsername(user.getUsername());
        response.setAvatar(user.getAvatar());
        response.setBio(user.getBio());
        response.setPhone(maskPhone(user.getPhone())); // 脱敏后的手机号
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

    /**
     * 更新用户信息
     * <p>
     * 修改用户资料（头像、昵称、简介等）。
     * 修改成功后会清除Redis缓存。
     * </p>
     *
     * @param userId  用户ID
     * @param request 更新请求参数
     * @throws BusinessException 如果用户不存在(40401)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UpdateUserInfoDTO request) {
        UserDO user = userMapper.selectById(userId);
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

    /**
     * 获取他人个人主页信息
     * <p>
     * 查看其他用户的公开资料及统计数据。
     * 同时返回当前用户对该用户的关注状态。
     * </p>
     *
     * @param userId        目标用户ID
     * @param currentUserId 当前登录用户ID（可选）
     * @return 用户主页VO
     * @throws BusinessException 如果用户不存在(40401)
     */
    @Override
    public UserProfileVO getUserProfile(Long userId, Long currentUserId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        UserStatsDO stats = userStatsMapper.selectByUserId(userId);

        UserProfileVO response = new UserProfileVO();
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
            response.setFollowing(isFollowing(currentUserId, userId));
        } else {
            response.setFollowing(false);
        }

        return response;
    }

    /**
     * 获取我的笔记列表
     * <p>
     * 查询当前用户发布的所有笔记，包括审核中和违规的笔记。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getMyNotes(Long userId, Integer pageNum, Integer pageSize) {
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        // 用户查看自己的笔记时，显示所有状态（包括审核中、已拒绝）
        // 状态说明：1=正常, 2=隐藏/拒绝, 3=审核中, 4=违规
        wrapper.eq(NoteDO::getUserId, userId)
                .in(NoteDO::getStatus, 1, 2, 3, 4) // 显示所有状态
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        List<NoteItemVO> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取我的收藏
     * <p>
     * 支持按类型筛选（笔记/店铺）。
     * 使用批量查询优化，避免 N+1 问题。
     * </p>
     *
     * @param userId   用户ID
     * @param type     类型 (1=笔记, 2=店铺)
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 收藏记录VO分页列表
     */
    @Override
    public PageResult<FavoriteItemVO> getMyFavorites(Long userId, Integer type, Integer pageNum, Integer pageSize) {
        Page<UserFavoriteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
                .eq(type != null, UserFavoriteDO::getType, type)
                .orderByDesc(UserFavoriteDO::getCreatedAt);

        Page<UserFavoriteDO> favPage = userFavoriteMapper.selectPage(page, wrapper);

        if (favPage.getRecords().isEmpty()) {
            return PageResult.of(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        // ========== 批量查询优化 (In-Memory Map Assembly) ==========

        // 1. 按类型分组收集 targetId
        List<Long> noteIds = favPage.getRecords().stream()
                .filter(f -> f.getType() == 1)
                .map(UserFavoriteDO::getTargetId)
                .collect(Collectors.toList());

        List<Long> shopIds = favPage.getRecords().stream()
                .filter(f -> f.getType() == 2)
                .map(UserFavoriteDO::getTargetId)
                .collect(Collectors.toList());

        // 2. 批量查询笔记和商店
        java.util.Map<Long, NoteDO> noteMap = new java.util.HashMap<>();
        if (!noteIds.isEmpty()) {
            List<NoteDO> notes = noteMapper.selectBatchIds(noteIds);
            noteMap = notes.stream().collect(Collectors.toMap(NoteDO::getId, n -> n, (a, b) -> a));
        }

        java.util.Map<Long, ShopDO> shopMap = new java.util.HashMap<>();
        if (!shopIds.isEmpty()) {
            List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);
            shopMap = shops.stream().collect(Collectors.toMap(ShopDO::getId, s -> s, (a, b) -> a));
        }

        // 3. 组装响应数据（无额外数据库查询）
        final java.util.Map<Long, NoteDO> finalNoteMap = noteMap;
        final java.util.Map<Long, ShopDO> finalShopMap = shopMap;

        List<FavoriteItemVO> list = favPage.getRecords().stream().map(fav -> {
            FavoriteItemVO item = new FavoriteItemVO();
            item.setId(fav.getId().toString());
            item.setType(fav.getType());
            item.setTargetId(fav.getTargetId().toString());
            item.setCreatedAt(fav.getCreatedAt().toString());

            if (fav.getType() == 1) {
                NoteDO note = finalNoteMap.get(fav.getTargetId());
                if (note != null) {
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                    item.setLikes(note.getLikeCount());
                }
            } else if (fav.getType() == 2) {
                ShopDO shop = finalShopMap.get(fav.getTargetId());
                if (shop != null) {
                    item.setImage(shop.getHeaderImage());
                    item.setTitle(shop.getName());
                    item.setLikes(0);
                }
            }
            return item;
        }).collect(Collectors.toList());

        return PageResult.of(list, favPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取浏览历史
     * <p>
     * 查询用户的浏览记录（笔记或店铺）。
     * 使用批量查询优化，避免 N+1 问题。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 历史记录VO分页列表
     */
    @Override
    public PageResult<HistoryItemVO> getBrowseHistory(Long userId, Integer pageNum, Integer pageSize) {
        Page<BrowseHistoryDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BrowseHistoryDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistoryDO::getUserId, userId)
                .orderByDesc(BrowseHistoryDO::getCreatedAt);

        Page<BrowseHistoryDO> historyPage = browseHistoryMapper.selectPage(page, wrapper);

        if (historyPage.getRecords().isEmpty()) {
            return PageResult.of(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        // ========== 批量查询优化 (In-Memory Map Assembly) ==========

        // 1. 按类型分组收集 targetId
        List<Long> noteIds = historyPage.getRecords().stream()
                .filter(h -> h.getType() == 1)
                .map(BrowseHistoryDO::getTargetId)
                .collect(Collectors.toList());

        List<Long> shopIds = historyPage.getRecords().stream()
                .filter(h -> h.getType() == 2)
                .map(BrowseHistoryDO::getTargetId)
                .collect(Collectors.toList());

        // 2. 批量查询笔记
        java.util.Map<Long, NoteDO> noteMap = new java.util.HashMap<>();
        java.util.Set<Long> authorIds = new java.util.HashSet<>();
        if (!noteIds.isEmpty()) {
            List<NoteDO> notes = noteMapper.selectBatchIds(noteIds);
            noteMap = notes.stream().collect(Collectors.toMap(NoteDO::getId, n -> n, (a, b) -> a));
            // 收集作者ID
            authorIds = notes.stream()
                    .map(NoteDO::getUserId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        // 3. 批量查询作者信息
        java.util.Map<Long, UserDO> authorMap = new java.util.HashMap<>();
        if (!authorIds.isEmpty()) {
            List<UserDO> authors = userMapper.selectBatchIds(authorIds);
            authorMap = authors.stream().collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));
        }

        // 4. 批量查询商店
        java.util.Map<Long, ShopDO> shopMap = new java.util.HashMap<>();
        if (!shopIds.isEmpty()) {
            List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);
            shopMap = shops.stream().collect(Collectors.toMap(ShopDO::getId, s -> s, (a, b) -> a));
        }

        // 5. 组装响应数据（无额外数据库查询）
        final java.util.Map<Long, NoteDO> finalNoteMap = noteMap;
        final java.util.Map<Long, UserDO> finalAuthorMap = authorMap;
        final java.util.Map<Long, ShopDO> finalShopMap = shopMap;

        List<HistoryItemVO> list = historyPage.getRecords().stream().map(history -> {
            HistoryItemVO item = new HistoryItemVO();
            item.setId(history.getId().toString());
            item.setType(history.getType());
            item.setTargetId(history.getTargetId().toString());
            item.setViewTime(TimeUtil.formatRelativeTime(history.getCreatedAt()));
            item.setCreatedAt(history.getCreatedAt().toString());

            if (history.getType() == 1) {
                NoteDO note = finalNoteMap.get(history.getTargetId());
                if (note != null) {
                    item.setImage(note.getCoverImage());
                    item.setTitle(note.getTitle());
                    // 从预查询的 Map 获取作者信息
                    if (note.getUserId() != null) {
                        UserDO author = finalAuthorMap.get(note.getUserId());
                        if (author != null) {
                            item.setAuthor(author.getUsername());
                            item.setAuthorId(author.getId());
                        }
                    }
                }
            } else if (history.getType() == 2) {
                ShopDO shop = finalShopMap.get(history.getTargetId());
                if (shop != null) {
                    item.setImage(shop.getHeaderImage());
                    item.setTitle(shop.getName());
                    item.setAuthor(shop.getName());
                }
            }
            return item;
        }).collect(Collectors.toList());

        return PageResult.of(list, historyPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 关注用户
     *
     * @param userId       当前用户ID
     * @param targetUserId 目标用户ID
     * @throws BusinessException 如果关注自己(40001)、用户不存在(40401)或已关注(40001)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(40001, "不能关注自己");
        }

        // 检查目标用户是否存在
        UserDO targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 检查是否已关注
        if (isFollowing(userId, targetUserId)) {
            throw new BusinessException(40001, "已关注该用户");
        }

        // 插入关注记录
        UserFollowDO follow = new UserFollowDO();
        follow.setUserId(userId);
        follow.setFollowUserId(targetUserId);
        userFollowMapper.insert(follow);

        // 更新统计数据
        userStatsMapper.incrementFollowingCount(userId);
        userStatsMapper.incrementFollowerCount(targetUserId);

        // 发送关注通知
        UserDO user = userMapper.selectById(userId);
        // 使用新的系统通知方法，包含发送者信息
        messageService.sendSystemNotice(targetUserId, userId, 3, userId,
                user.getUsername() + " 关注了你", user.getAvatar());
    }

    /**
     * 取消关注用户
     *
     * @param userId       当前用户ID
     * @param targetUserId 目标用户ID
     * @throws BusinessException 如果未关注(40001)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long userId, Long targetUserId) {
        // 检查是否已关注
        LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollowDO::getUserId, userId)
                .eq(UserFollowDO::getFollowUserId, targetUserId);

        UserFollowDO follow = userFollowMapper.selectOne(wrapper);
        if (follow == null) {
            throw new BusinessException(40001, "未关注该用户");
        }

        // 删除关注记录
        userFollowMapper.deleteById(follow.getId());

        // 更新统计数据
        userStatsMapper.decrementFollowingCount(userId);
        userStatsMapper.decrementFollowerCount(targetUserId);
    }

    /**
     * 获取关注列表
     * <p>
     * 使用批量查询优化，避免 N+1 问题。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 用户列表VO
     */
    @Override
    public PageResult<UserItemVO> getFollowingList(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserFollowDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollowDO::getUserId, userId)
                .orderByDesc(UserFollowDO::getCreatedAt);

        Page<UserFollowDO> followPage = userFollowMapper.selectPage(page, wrapper);

        if (followPage.getRecords().isEmpty()) {
            return PageResult.of(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        // 批量查询用户信息
        List<Long> userIds = followPage.getRecords().stream()
                .map(UserFollowDO::getFollowUserId)
                .collect(Collectors.toList());

        List<UserDO> users = userMapper.selectBatchIds(userIds);
        java.util.Map<Long, UserDO> userMap = users.stream()
                .collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));

        List<UserItemVO> list = followPage.getRecords().stream()
                .map(f -> {
                    UserDO user = userMap.get(f.getFollowUserId());
                    UserItemVO item = convertToUserItem(user);
                    // 关注列表中的所有用户都是已关注的
                    if (item != null) {
                        item.setFollowing(true);
                    }
                    return item;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return PageResult.of(list, followPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取粉丝列表
     * <p>
     * 同时会返回当前用户是否回关了这些粉丝。
     * 使用批量查询优化，避免 N+1 问题。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 用户列表VO
     */
    @Override
    public PageResult<UserItemVO> getFollowerList(Long userId, Integer pageNum, Integer pageSize) {
        Page<UserFollowDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollowDO::getFollowUserId, userId)
                .orderByDesc(UserFollowDO::getCreatedAt);

        Page<UserFollowDO> followPage = userFollowMapper.selectPage(page, wrapper);

        if (followPage.getRecords().isEmpty()) {
            return PageResult.of(new ArrayList<>(), 0L, pageNum, pageSize);
        }

        // 1. 收集所有粉丝的用户ID
        List<Long> followerUserIds = followPage.getRecords().stream()
                .map(UserFollowDO::getUserId)
                .collect(Collectors.toList());

        // 2. 批量查询粉丝用户信息
        List<UserDO> users = userMapper.selectBatchIds(followerUserIds);
        java.util.Map<Long, UserDO> userMap = users.stream()
                .collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));

        // 3. 批量查询当前用户是否回关了这些粉丝（关注状态）
        LambdaQueryWrapper<UserFollowDO> followBackWrapper = new LambdaQueryWrapper<>();
        followBackWrapper.eq(UserFollowDO::getUserId, userId)
                .in(UserFollowDO::getFollowUserId, followerUserIds);
        List<UserFollowDO> followBackList = userFollowMapper.selectList(followBackWrapper);
        java.util.Set<Long> followingSet = followBackList.stream()
                .map(UserFollowDO::getFollowUserId)
                .collect(Collectors.toSet());

        // 4. 组装响应数据
        List<UserItemVO> list = followPage.getRecords().stream()
                .map(f -> {
                    UserDO user = userMap.get(f.getUserId());
                    UserItemVO item = convertToUserItem(user);
                    if (item != null && user != null) {
                        // 从预查询的 Set 判断是否回关
                        item.setFollowing(followingSet.contains(user.getId()));
                    }
                    return item;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return PageResult.of(list, followPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollowDO::getUserId, userId)
                .eq(UserFollowDO::getFollowUserId, targetUserId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    private NoteItemVO convertToNoteItem(NoteDO note) {
        NoteItemVO item = new NoteItemVO();
        item.setId(note.getId().toString());
        item.setImage(note.getCoverImage());
        item.setTitle(note.getTitle());
        item.setLikes(note.getLikeCount());
        item.setViews(note.getViewCount());
        item.setCreatedAt(note.getCreatedAt().toString());
        item.setStatus(note.getStatus());

        // 根据状态设置标签（供前端显示）
        switch (note.getStatus()) {
            case 1:
                // 正常状态，不需要特殊标签
                break;
            case 2:
                item.setTag("已隐藏");
                item.setTagClass("tag-hidden");
                break;
            case 3:
                item.setTag("审核中");
                item.setTagClass("tag-pending");
                break;
            case 4:
                item.setTag("违规");
                item.setTagClass("tag-rejected");
                break;
            default:
                break;
        }

        return item;
    }

    private UserItemVO convertToUserItem(UserDO user) {
        if (user == null) {
            return null;
        }
        UserItemVO item = new UserItemVO();
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

    /**
     * 修改密码
     * <p>
     * 需要验证手机验证码和旧密码。
     * 修改成功后会清除Redis缓存。
     * </p>
     *
     * @param userId  用户ID
     * @param request 修改密码请求（含旧密码、新密码、验证码）
     * @throws BusinessException 如果验证失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO request) {
        UserDO user = userMapper.selectById(userId);
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
     * 更换绑定手机号
     * <p>
     * 需要验证旧手机号验证码和新手机号验证码。
     * 24小时内只能修改一次。
     * </p>
     *
     * @param userId  用户ID
     * @param request 换绑请求（含新旧手机号及验证码）
     * @throws BusinessException 如果验证失败或频率超限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePhone(Long userId, ChangePhoneDTO request) {
        UserDO user = userMapper.selectById(userId);
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
        UserDO existingUser = userMapper.selectByPhone(newPhone);
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
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }
        return user.getPhone();
    }
}

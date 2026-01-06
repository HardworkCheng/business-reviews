package com.businessreviews.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.common.PageResult;
import com.businessreviews.enums.NoteStatus;
import com.businessreviews.event.NoteCreatedEvent;
import com.businessreviews.model.dto.app.PublishNoteDTO;
import com.businessreviews.model.vo.NoteDetailVO;
import com.businessreviews.model.vo.NoteDetailVO.TopicInfo;
import com.businessreviews.model.vo.NoteItemVO;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.app.MessageService;
import com.businessreviews.service.app.NoteService;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 笔记服务实现类
 * <p>
 * 处理C端用户笔记的核心业务逻辑。
 * 核心功能包括：
 * 1. 笔记列表查询（推荐、附近、关注、个人、收藏）
 * 2. 笔记详情查询（含关联信息预加载）
 * 3. 笔记互动（点赞、收藏、浏览）
 * 4. 笔记发布与管理（CRUD、异步审核）
 * <p>
 * 性能优化：使用 In-Memory Map Assembly 模式解决 N+1 查询问题。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, NoteDO> implements NoteService {

    private final NoteMapper noteMapper;
    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;
    private final NoteTagMapper noteTagMapper;
    private final NoteTopicMapper noteTopicMapper;
    private final UserNoteLikeMapper userNoteLikeMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final BrowseHistoryMapper browseHistoryMapper;
    private final UserFollowMapper userFollowMapper;
    private final ShopMapper shopMapper;
    private final TagMapper tagMapper;
    private final TopicMapper topicMapper;
    private final MerchantMapper merchantMapper;
    private final RedisUtil redisUtil;
    private final MessageService messageService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 获取首页推荐笔记
     * <p>
     * 混合查询用户笔记和商家笔记。
     * 排序规则：优先按推荐值(recommend)降序，其次按点赞数降序，最后按创建时间降序。
     * </p>
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getRecommendedNotes(Integer pageNum, Integer pageSize) {
        // 尝试从缓存获取
        String cacheKey = RedisKeyConstants.NOTES_RECOMMENDED + pageNum;

        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getStatus, 1) // 只显示已发布的笔记
                .in(NoteDO::getNoteType, 1, 2) // 包含用户笔记和商家笔记
                .orderByDesc(NoteDO::getRecommend) // 推荐笔记优先
                .orderByDesc(NoteDO::getLikeCount)
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(notePage.getRecords());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取用户的笔记列表
     * <p>
     * 查询指定用户发布的已发布状态(status=1)笔记。
     * 按创建时间倒序排列。
     * </p>
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getUserNotes(Long userId, Integer pageNum, Integer pageSize) {
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getUserId, userId)
                .eq(NoteDO::getStatus, 1)
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(notePage.getRecords());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取用户点赞的笔记列表
     * <p>
     * 查询用户点赞过的笔记，按点赞时间倒序排列。
     * 仅显示当前状态为已发布(status=1)的笔记。
     * </p>
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getLikedNotes(Long userId, Integer pageNum, Integer pageSize) {
        // 获取用户点赞的笔记ID列表
        LambdaQueryWrapper<UserNoteLikeDO> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserNoteLikeDO::getUserId, userId)
                .orderByDesc(UserNoteLikeDO::getCreatedAt);

        // 先查询总数
        Long total = userNoteLikeMapper.selectCount(likeWrapper);
        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 分页查询点赞记录
        Page<UserNoteLikeDO> likePage = new Page<>(pageNum, pageSize);
        Page<UserNoteLikeDO> resultPage = userNoteLikeMapper.selectPage(likePage, likeWrapper);

        if (resultPage.getRecords().isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        // 获取笔记ID列表
        List<Long> noteIds = resultPage.getRecords().stream()
                .map(UserNoteLikeDO::getNoteId)
                .collect(Collectors.toList());

        // 查询笔记详情
        LambdaQueryWrapper<NoteDO> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(NoteDO::getId, noteIds)
                .eq(NoteDO::getStatus, 1);
        List<NoteDO> notes = noteMapper.selectList(noteWrapper);

        // 按原有顺序排序笔记（按点赞时间）
        Map<Long, NoteDO> noteMap = notes.stream()
                .collect(Collectors.toMap(NoteDO::getId, note -> note));

        // 过滤出有效的笔记并保持原有顺序
        List<NoteDO> orderedNotes = noteIds.stream()
                .map(noteMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(orderedNotes);

        return PageResult.of(list, total, pageNum, pageSize);
    }

    /**
     * 获取发现页笔记（按分类或排序）
     * <p>
     * 支持多种排序方式：
     * - hot: 按热度（点赞数）降序
     * - new: 按发布时间倒序
     * - default: 综合排序（热度+时间）
     * </p>
     *
     * @param categoryId 分类ID（可选）
     * @param sortBy     排序方式 (hot/new/default)
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getExploreNotes(Long categoryId, String sortBy, Integer pageNum, Integer pageSize) {
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getStatus, 1);

        if (categoryId != null) {
            // 暂不支持按分类筛选
        }

        // 排序
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(NoteDO::getLikeCount);
        } else if ("new".equals(sortBy)) {
            wrapper.orderByDesc(NoteDO::getCreatedAt);
        } else {
            wrapper.orderByDesc(NoteDO::getLikeCount).orderByDesc(NoteDO::getCreatedAt);
        }

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(notePage.getRecords());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取附近笔记
     * <p>
     * 查询带有地理位置信息的笔记。
     * 当前实现为简化版，未进行实际距离计算过滤，仅按时间排序。
     * </p>
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param distance  搜索半径(km)
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getNearbyNotes(Double latitude, Double longitude, Double distance, Integer pageNum,
            Integer pageSize) {
        // 简化实现，实际应使用地理位置查询
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getStatus, 1)
                .isNotNull(NoteDO::getLatitude)
                .isNotNull(NoteDO::getLongitude)
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(notePage.getRecords());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取关注人笔记
     * <p>
     * 查询当前用户关注的所有用户发布的笔记。
     * 按创建时间倒序排列。
     * </p>
     *
     * @param userId   当前用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 笔记VO分页列表
     */
    @Override
    public PageResult<NoteItemVO> getFollowingNotes(Long userId, Integer pageNum, Integer pageSize) {
        // 获取关注的用户ID列表
        LambdaQueryWrapper<UserFollowDO> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(UserFollowDO::getUserId, userId);
        List<UserFollowDO> follows = userFollowMapper.selectList(followWrapper);

        if (follows.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }

        List<Long> followingIds = follows.stream()
                .map(UserFollowDO::getFollowUserId)
                .collect(Collectors.toList());

        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getStatus, 1)
                .in(NoteDO::getUserId, followingIds)
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(notePage.getRecords());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取笔记详情
     * <p>
     * 返回笔记完整信息，包括作者、内容、图片、位置、话题等。
     * 同时返回当前用户对该笔记的互动状态（点赞、收藏、关注作者）。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可选）
     * @return 笔记详情VO
     * @throws BusinessException 如果笔记不存在或已下架(40402)
     */
    @Override
    public NoteDetailVO getNoteDetail(Long noteId, Long userId) {
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }

        // 查询作者信息
        UserDO author = userMapper.selectById(note.getUserId());

        // 查询标签
        List<String> tags = getNoteTagNames(noteId);

        // 构建响应
        NoteDetailVO response = new NoteDetailVO();
        response.setId(note.getId());
        response.setImage(note.getCoverImage());
        response.setImages(parseImages(note.getImages()));
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setAuthor(author != null ? author.getUsername() : "未知用户");
        response.setAuthorAvatar(author != null ? author.getAvatar() : null);
        response.setAuthorId(note.getUserId());
        // 使用更新时间而不是创建时间，这样编辑后时间会正确显示
        response.setPublishTime(
                TimeUtil.formatRelativeTime(note.getUpdatedAt() != null ? note.getUpdatedAt() : note.getCreatedAt()));
        response.setTags(tags);
        response.setLikeCount(note.getLikeCount());
        response.setCommentCount(note.getCommentCount());
        response.setViewCount(note.getViewCount());
        response.setFavoriteCount(note.getFavoriteCount());
        response.setLocation(note.getLocation());
        // 使用updatedAt来显示最后更新时间
        response.setCreatedAt(note.getUpdatedAt() != null ? note.getUpdatedAt() : note.getCreatedAt());

        // 查询关联商家
        if (note.getShopId() != null) {
            ShopDO shop = shopMapper.selectById(note.getShopId());
            if (shop != null) {
                response.setShopId(shop.getId());
                response.setShopName(shop.getName());
            }
        }

        // 查询话题信息
        List<TopicInfo> topics = getNoteTopics(noteId);
        response.setTopics(topics);

        // 设置位置坐标
        response.setLatitude(note.getLatitude());
        response.setLongitude(note.getLongitude());

        // 查询用户互动状态
        if (userId != null) {
            boolean liked = isLiked(userId, noteId);
            boolean bookmarked = isBookmarked(userId, noteId);
            boolean following = isFollowing(userId, note.getUserId());

            log.info("笔记详情查询 - 用户ID: {}, 笔记ID: {}, 点赞: {}, 收藏: {}, 关注: {}",
                    userId, noteId, liked, bookmarked, following);

            response.setLiked(liked);
            response.setBookmarked(bookmarked);
            response.setFollowing(following);
            response.setSelfAuthor(userId.equals(note.getUserId()));
        } else {
            response.setLiked(false);
            response.setBookmarked(false);
            response.setFollowing(false);
            response.setSelfAuthor(false);
        }

        return response;
    }

    /**
     * 发布笔记
     * <p>
     * 1. 保存笔记基础信息
     * 2. 识别是否为商家笔记
     * 3. 异步触发AI内容审核
     * 4. 保存标签和话题关联
     * 5. 更新用户发布统计
     * </p>
     *
     * @param userId  当前用户ID
     * @param request 发布请求参数
     * @return 新建笔记ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishNote(Long userId, PublishNoteDTO request) {
        NoteDO note = new NoteDO();
        note.setUserId(userId);

        // 如果没有标题，使用内容前20个字作为标题
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            String content = request.getContent();
            note.setTitle(content.length() > 20 ? content.substring(0, 20) + "..." : content);
        } else {
            note.setTitle(request.getTitle().trim());
        }

        note.setContent(request.getContent());
        note.setCoverImage(request.getImages() != null && !request.getImages().isEmpty()
                ? request.getImages().get(0)
                : null);
        note.setImages(String.join(",", request.getImages()));
        note.setShopId(request.getShopId());
        note.setLocation(request.getLocation());
        note.setLatitude(request.getLatitude());
        note.setLongitude(request.getLongitude());
        // 新发布的笔记默认状态为审核中(PENDING=3)，由异步AI审核决定最终状态
        // 如果用户指定隐藏(status=2)，则直接使用用户指定的状态
        note.setStatus(request.getStatus() != null && request.getStatus() == 2
                ? NoteStatus.HIDDEN.getCode()
                : NoteStatus.PENDING.getCode());
        note.setLikeCount(0);
        note.setCommentCount(0);
        note.setViewCount(0);
        note.setFavoriteCount(0);

        // 检查用户是否是商家账号，如果是则设置商家笔记标识
        Long merchantId = findMerchantIdByUserId(userId);
        if (merchantId != null) {
            note.setNoteType(2); // 商家笔记
            note.setMerchantId(merchantId);
            note.setSyncStatus(1); // 已同步
            log.info("用户{}是商家账号，设置为商家笔记，merchantId={}", userId, merchantId);
        } else {
            note.setNoteType(1); // 用户笔记
        }

        noteMapper.insert(note);

        // 保存标签关联
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            saveNoteTags(note.getId(), request.getTags());
        }

        // 保存话题关联 - 优先使用topicNames（自定义话题），否则使用topics（ID列表）
        if (request.getTopicNames() != null && !request.getTopicNames().isEmpty()) {
            saveNoteTopicsByNames(note.getId(), request.getTopicNames());
        } else if (request.getTopics() != null && !request.getTopics().isEmpty()) {
            saveNoteTopics(note.getId(), request.getTopics());
        }

        // 更新用户笔记数
        userStatsMapper.incrementNoteCount(userId);

        log.info("用户{}发布笔记成功（审核中），笔记ID={}", userId, note.getId());

        // 发布笔记创建事件，触发异步AI内容审核
        // 事件驱动设计：解耦发布逻辑与审核逻辑，用户无需等待审核完成
        if (note.getStatus() == NoteStatus.PENDING.getCode()) {
            eventPublisher.publishEvent(new NoteCreatedEvent(
                    this,
                    note.getId(),
                    note.getTitle(),
                    note.getContent(),
                    userId));
            log.info("已发布审核事件，笔记ID={}将进行异步AI审核", note.getId());
        }

        return note.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(Long userId, Long noteId, PublishNoteDTO request) {
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BusinessException(40300, "无权限操作");
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            note.setCoverImage(request.getImages().get(0));
            note.setImages(String.join(",", request.getImages()));
        }
        note.setShopId(request.getShopId());
        note.setLocation(request.getLocation());
        note.setLatitude(request.getLatitude());
        note.setLongitude(request.getLongitude());

        noteMapper.updateById(note);

        // 更新标签关联
        if (request.getTags() != null) {
            deleteNoteTags(noteId);
            if (!request.getTags().isEmpty()) {
                saveNoteTags(noteId, request.getTags());
            }
        }

        // 更新话题关联 - 优先使用topicNames（自定义话题），否则使用topics（ID列表）
        if (request.getTopicNames() != null) {
            deleteNoteTopics(noteId);
            if (!request.getTopicNames().isEmpty()) {
                saveNoteTopicsByNames(noteId, request.getTopicNames());
            }
        } else if (request.getTopics() != null) {
            deleteNoteTopics(noteId);
            if (!request.getTopics().isEmpty()) {
                saveNoteTopics(noteId, request.getTopics());
            }
        }

        log.info("用户{}更新笔记成功，笔记ID={}", userId, noteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long userId, Long noteId) {
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40402, "笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(40300, "无权限操作");
        }

        // 软删除
        note.setStatus(0);
        noteMapper.updateById(note);

        // 更新用户笔记数
        userStatsMapper.decrementNoteCount(userId);
    }

    /**
     * 点赞笔记
     * <p>
     * 用户对笔记进行点赞。如果已点赞则自动切换为取消点赞。
     * 点赞成功后会发送通知给作者。
     * </p>
     *
     * @param userId 当前用户ID
     * @param noteId 笔记ID
     * @return 最新的点赞数
     * @throws BusinessException 如果笔记不存在或已下架(40402)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer likeNote(Long userId, Long noteId) {
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }

        // 检查是否已点赞，如果已点赞则取消点赞
        if (isLiked(userId, noteId)) {
            log.info("用户 {} 取消点赞笔记 {}", userId, noteId);
            return unlikeNote(userId, noteId);
        }

        // 插入点赞记录
        UserNoteLikeDO like = new UserNoteLikeDO();
        like.setUserId(userId);
        like.setNoteId(noteId);
        userNoteLikeMapper.insert(like);

        // 更新笔记点赞数
        noteMapper.incrementLikeCount(noteId);

        // 更新作者获赞数
        userStatsMapper.incrementLikeCount(note.getUserId());

        // 发送点赞通知
        if (!userId.equals(note.getUserId())) {
            UserDO user = userMapper.selectById(userId);
            // 使用新的系统通知方法，包含发送者信息和笔记图片
            messageService.sendSystemNotice(note.getUserId(), userId, 1, noteId,
                    user.getUsername() + " 赞了你的笔记", note.getCoverImage());
        }

        log.info("用户 {} 点赞笔记 {}", userId, noteId);
        return note.getLikeCount() + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeNote(Long userId, Long noteId) {
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40402, "笔记不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<UserNoteLikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoteLikeDO::getUserId, userId)
                .eq(UserNoteLikeDO::getNoteId, noteId);
        UserNoteLikeDO like = userNoteLikeMapper.selectOne(wrapper);

        if (like == null) {
            throw new BusinessException(40001, "未点赞");
        }

        // 删除点赞记录
        userNoteLikeMapper.deleteById(like.getId());

        // 更新笔记点赞数
        noteMapper.decrementLikeCount(noteId);

        // 更新作者获赞数
        userStatsMapper.decrementLikeCount(note.getUserId());

        return Math.max(0, note.getLikeCount() - 1);
    }

    /**
     * 收藏笔记
     * <p>
     * 用户收藏笔记。如果已收藏则自动切换为取消收藏。
     * </p>
     *
     * @param userId 当前用户ID
     * @param noteId 笔记ID
     * @throws BusinessException 如果笔记不存在或已下架(40402)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookmarkNote(Long userId, Long noteId) {
        NoteDO note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }

        // 检查是否已收藏，如果已收藏则取消收藏
        if (isBookmarked(userId, noteId)) {
            log.info("用户 {} 取消收藏笔记 {}", userId, noteId);
            unbookmarkNote(userId, noteId);
            return;
        }

        // 插入收藏记录
        UserFavoriteDO favorite = new UserFavoriteDO();
        favorite.setUserId(userId);
        favorite.setType(1); // 笔记类型
        favorite.setTargetId(noteId);
        userFavoriteMapper.insert(favorite);

        // 更新笔记收藏数
        noteMapper.incrementFavoriteCount(noteId);

        // 更新用户收藏数
        userStatsMapper.incrementFavoriteCount(userId);

        log.info("用户 {} 收藏笔记 {}", userId, noteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbookmarkNote(Long userId, Long noteId) {
        // 检查是否已收藏
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
                .eq(UserFavoriteDO::getType, 1)
                .eq(UserFavoriteDO::getTargetId, noteId);
        UserFavoriteDO favorite = userFavoriteMapper.selectOne(wrapper);

        if (favorite == null) {
            throw new BusinessException(40001, "未收藏");
        }

        // 删除收藏记录
        userFavoriteMapper.deleteById(favorite.getId());

        // 更新笔记收藏数
        noteMapper.decrementFavoriteCount(noteId);

        // 更新用户收藏数
        userStatsMapper.decrementFavoriteCount(userId);
    }

    /**
     * 增加笔记浏览量 (异步)
     * <p>
     * 增加浏览计数并记录用户的浏览历史。
     * </p>
     *
     * @param noteId 笔记ID
     * @param userId 当前用户ID（可选，未登录不记录历史）
     */
    @Override
    @Async("asyncExecutor")
    public void increaseViewCount(Long noteId, Long userId) {
        log.info("增加浏览量 - 笔记ID: {}, 用户ID: {}", noteId, userId);
        noteMapper.incrementViewCount(noteId);

        // 记录浏览历史（仅当用户已登录时）
        if (userId != null) {
            try {
                log.info("开始记录浏览历史 - 用户ID: {}, 笔记ID: {}", userId, noteId);

                // 检查是否已有相同的浏览记录（同一用户、同一笔记）
                LambdaQueryWrapper<BrowseHistoryDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BrowseHistoryDO::getUserId, userId)
                        .eq(BrowseHistoryDO::getType, 1) // 1=笔记
                        .eq(BrowseHistoryDO::getTargetId, noteId);
                BrowseHistoryDO existing = browseHistoryMapper.selectOne(wrapper);

                if (existing != null) {
                    // 更新浏览时间
                    log.info("更新已有浏览记录 - 记录ID: {}", existing.getId());
                    existing.setCreatedAt(LocalDateTime.now());
                    browseHistoryMapper.updateById(existing);
                } else {
                    // 插入新记录
                    log.info("插入新浏览记录");
                    BrowseHistoryDO history = new BrowseHistoryDO();
                    history.setUserId(userId);
                    history.setType(1); // 1=笔记
                    history.setTargetId(noteId);
                    history.setCreatedAt(LocalDateTime.now());
                    int result = browseHistoryMapper.insert(history);
                    log.info("浏览记录插入结果: {}, 新记录ID: {}", result, history.getId());
                }
                log.info("浏览历史记录成功 - 用户ID: {}, 笔记ID: {}", userId, noteId);
            } catch (Exception e) {
                log.error("记录浏览历史失败 - 用户ID: {}, 笔记ID: {}, 错误: {}", userId, noteId, e.getMessage(), e);
            }
        } else {
            log.info("用户未登录，不记录浏览历史 - 笔记ID: {}", noteId);
        }
    }

    @Override
    public boolean isLiked(Long userId, Long noteId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<UserNoteLikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoteLikeDO::getUserId, userId)
                .eq(UserNoteLikeDO::getNoteId, noteId);
        long count = userNoteLikeMapper.selectCount(wrapper);
        boolean result = count > 0;
        log.debug("检查点赞状态 - 用户ID: {}, 笔记ID: {}, 记录数: {}, 结果: {}", userId, noteId, count, result);
        return result;
    }

    @Override
    public boolean isBookmarked(Long userId, Long noteId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<UserFavoriteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavoriteDO::getUserId, userId)
                .eq(UserFavoriteDO::getType, 1)
                .eq(UserFavoriteDO::getTargetId, noteId);
        long count = userFavoriteMapper.selectCount(wrapper);
        boolean result = count > 0;
        log.debug("检查收藏状态 - 用户ID: {}, 笔记ID: {}, 记录数: {}, 结果: {}", userId, noteId, count, result);
        return result;
    }

    @Override
    public PageResult<NoteItemVO> searchNotes(String keyword, Integer pageNum, Integer pageSize) {
        Page<NoteDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteDO::getStatus, 1)
                .and(w -> w.like(NoteDO::getTitle, keyword)
                        .or()
                        .like(NoteDO::getContent, keyword))
                .orderByDesc(NoteDO::getCreatedAt);

        Page<NoteDO> notePage = noteMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<NoteItemVO> list = convertNoteList(notePage.getRecords());

        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    /**
     * 批量转换笔记列表 - 解决N+1查询问题
     * 使用In-Memory Map预加载关联数据，将查询复杂度从O(N)降为O(1)
     * 
     * @param notes 笔记列表
     * @return 转换后的VO列表
     */
    private List<NoteItemVO> convertNoteList(List<NoteDO> notes) {
        if (notes == null || notes.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 提取所有涉及的用户ID和店铺ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> shopIds = new HashSet<>();
        for (NoteDO note : notes) {
            if (note.getUserId() != null) {
                userIds.add(note.getUserId());
            }
            // 只有商家笔记才需要查询店铺信息
            if (note.getNoteType() != null && note.getNoteType() == 2 && note.getShopId() != null) {
                shopIds.add(note.getShopId());
            }
        }

        // 2. 批量查询用户信息，转为Map<userId, UserDO>
        Map<Long, UserDO> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        }

        // 3. 批量查询店铺信息，转为Map<shopId, ShopDO>
        Map<Long, ShopDO> shopMap = Collections.emptyMap();
        if (!shopIds.isEmpty()) {
            List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);
            shopMap = shops.stream()
                    .collect(Collectors.toMap(ShopDO::getId, Function.identity()));
        }

        // 4. 使用预加载的Map进行转换
        final Map<Long, UserDO> finalUserMap = userMap;
        final Map<Long, ShopDO> finalShopMap = shopMap;
        return notes.stream()
                .map(note -> convertToNoteItem(note, finalUserMap, finalShopMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换单个笔记 - 使用预加载的Map获取关联数据
     * 
     * @param note    笔记实体
     * @param userMap 预加载的用户Map
     * @param shopMap 预加载的店铺Map
     * @return 笔记VO
     */
    private NoteItemVO convertToNoteItem(NoteDO note, Map<Long, UserDO> userMap, Map<Long, ShopDO> shopMap) {
        NoteItemVO item = new NoteItemVO();
        item.setId(note.getId().toString());
        item.setImage(note.getCoverImage());
        item.setTitle(note.getTitle());
        item.setLikes(note.getLikeCount());
        item.setViews(note.getViewCount());
        item.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);

        // 从预加载的Map获取作者信息，O(1)复杂度
        UserDO author = userMap.get(note.getUserId());
        if (author != null) {
            item.setAuthor(author.getUsername());
            item.setAuthorAvatar(author.getAvatar());
            item.setAuthorId(author.getId().toString());
        }

        // 设置笔记类型
        item.setNoteType(note.getNoteType() != null ? note.getNoteType() : 1);

        // 处理商家笔记标识
        if (note.getNoteType() != null && note.getNoteType() == 2) {
            // 商家笔记
            item.setTag("商家");
            item.setTagClass("tag-merchant");

            // 从预加载的Map获取店铺信息，O(1)复杂度
            if (note.getShopId() != null) {
                ShopDO shop = shopMap.get(note.getShopId());
                if (shop != null) {
                    item.setShopId(shop.getId().toString());
                    item.setShopName(shop.getName());
                }
            }
        } else {
            // 用户笔记，计算热度标签
            if (note.getLikeCount() > 1000) {
                item.setTag("热门");
                item.setTagClass("tag-hot");
            } else if (note.getCreatedAt() != null &&
                    note.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
                item.setTag("新发");
                item.setTagClass("tag-new");
            }
        }

        return item;
    }

    private List<String> getNoteTagNames(Long noteId) {
        LambdaQueryWrapper<NoteTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTagDO::getNoteId, noteId);
        List<NoteTagDO> noteTags = noteTagMapper.selectList(wrapper);

        return noteTags.stream()
                .map(NoteTagDO::getTagName)
                .filter(name -> name != null)
                .collect(Collectors.toList());
    }

    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(images.split(","));
    }

    private void saveNoteTags(Long noteId, List<String> tags) {
        for (String tagName : tags) {
            // 创建关联
            NoteTagDO noteTag = new NoteTagDO();
            noteTag.setNoteId(noteId);
            noteTag.setTagName(tagName);
            noteTagMapper.insert(noteTag);
        }
    }

    private void saveNoteTopics(Long noteId, List<Long> topics) {
        for (Long topicId : topics) {
            NoteTopicDO noteTopic = new NoteTopicDO();
            noteTopic.setNoteId(noteId);
            noteTopic.setTopicId(topicId);
            noteTopicMapper.insert(noteTopic);
        }
    }

    /**
     * 通过话题名称保存笔记话题关联（支持自定义话题）
     * 如果话题不存在，则自动创建
     */
    private void saveNoteTopicsByNames(Long noteId, List<String> topicNames) {
        for (String topicName : topicNames) {
            if (topicName == null || topicName.trim().isEmpty()) {
                continue;
            }

            // 查找或创建话题
            TopicDO topic = findOrCreateTopic(topicName.trim());

            // 创建笔记话题关联
            NoteTopicDO noteTopic = new NoteTopicDO();
            noteTopic.setNoteId(noteId);
            noteTopic.setTopicId(topic.getId());
            noteTopicMapper.insert(noteTopic);

            // 更新话题的笔记数量
            topicMapper.incrementNoteCount(topic.getId());
        }
    }

    /**
     * 查找或创建话题
     * 如果话题已存在则返回，否则创建新话题
     */
    private TopicDO findOrCreateTopic(String topicName) {
        // 查找已存在的话题
        LambdaQueryWrapper<TopicDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicDO::getName, topicName)
                .eq(TopicDO::getStatus, 1);
        TopicDO topic = topicMapper.selectOne(wrapper);

        if (topic == null) {
            // 创建新话题
            topic = new TopicDO();
            topic.setName(topicName);
            topic.setStatus(1);
            topic.setHot(0);
            topic.setNoteCount(0);
            topic.setViewCount(0);
            topicMapper.insert(topic);
            log.info("创建新话题: {}, ID: {}", topicName, topic.getId());
        }

        return topic;
    }

    private void deleteNoteTags(Long noteId) {
        LambdaQueryWrapper<NoteTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTagDO::getNoteId, noteId);
        noteTagMapper.delete(wrapper);
    }

    private void deleteNoteTopics(Long noteId) {
        LambdaQueryWrapper<NoteTopicDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTopicDO::getNoteId, noteId);
        noteTopicMapper.delete(wrapper);
    }

    /**
     * 获取笔记关联的话题列表
     */
    private List<TopicInfo> getNoteTopics(Long noteId) {
        LambdaQueryWrapper<NoteTopicDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTopicDO::getNoteId, noteId);
        List<NoteTopicDO> noteTopics = noteTopicMapper.selectList(wrapper);

        if (noteTopics.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> topicIds = noteTopics.stream()
                .map(NoteTopicDO::getTopicId)
                .collect(Collectors.toList());

        List<TopicDO> topics = topicMapper.selectBatchIds(topicIds);

        return topics.stream()
                .map(topic -> {
                    TopicInfo info = new TopicInfo();
                    info.setId(topic.getId());
                    info.setName(topic.getName());
                    return info;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查是否关注
     */
    private boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }

        LambdaQueryWrapper<UserFollowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollowDO::getUserId, userId)
                .eq(UserFollowDO::getFollowUserId, targetUserId);
        long count = userFollowMapper.selectCount(wrapper);
        boolean result = count > 0;
        log.debug("检查关注状态 - 用户ID: {}, 目标用户ID: {}, 记录数: {}, 结果: {}", userId, targetUserId, count, result);
        return result;
    }

    /**
     * 根据用户ID查找关联的商家ID
     * 通过用户手机号匹配商家的联系电话
     */
    private Long findMerchantIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        // 查询用户信息
        UserDO user = userMapper.selectById(userId);
        if (user == null || user.getPhone() == null) {
            return null;
        }

        // 通过手机号查找商家
        LambdaQueryWrapper<MerchantDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantDO::getContactPhone, user.getPhone())
                .eq(MerchantDO::getStatus, 1); // 只查找正常状态的商家
        MerchantDO merchant = merchantMapper.selectOne(wrapper);

        if (merchant != null) {
            log.info("用户{}关联到商家: merchantId={}, merchantName={}", userId, merchant.getId(), merchant.getName());
            return merchant.getId();
        }

        return null;
    }
}

package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.Constants;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.PublishNoteRequest;
import com.businessreviews.dto.response.NoteDetailResponse;
import com.businessreviews.dto.response.NoteItemResponse;
import com.businessreviews.entity.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.MessageService;
import com.businessreviews.service.NoteService;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

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
    private final RedisUtil redisUtil;
    private final MessageService messageService;

    @Override
    public PageResult<NoteItemResponse> getRecommendedNotes(Integer pageNum, Integer pageSize) {
        // 尝试从缓存获取
        String cacheKey = Constants.RedisKey.NOTES_RECOMMENDED + pageNum;
        
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1)
               .orderByDesc(Note::getLikeCount)
               .orderByDesc(Note::getCreatedAt);
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<NoteItemResponse> getUserNotes(Long userId, Integer pageNum, Integer pageSize) {
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
    public PageResult<NoteItemResponse> getLikedNotes(Long userId, Integer pageNum, Integer pageSize) {
        // 获取用户点赞的笔记ID列表
        LambdaQueryWrapper<UserNoteLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserNoteLike::getUserId, userId)
                   .orderByDesc(UserNoteLike::getCreatedAt);
        
        // 先查询总数
        Long total = userNoteLikeMapper.selectCount(likeWrapper);
        if (total == 0) {
            return PageResult.empty(pageNum, pageSize);
        }
        
        // 分页查询点赞记录
        Page<UserNoteLike> likePage = new Page<>(pageNum, pageSize);
        Page<UserNoteLike> resultPage = userNoteLikeMapper.selectPage(likePage, likeWrapper);
        
        if (resultPage.getRecords().isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }
        
        // 获取笔记ID列表
        List<Long> noteIds = resultPage.getRecords().stream()
                .map(UserNoteLike::getNoteId)
                .collect(Collectors.toList());
        
        // 查询笔记详情
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getId, noteIds)
                   .eq(Note::getStatus, 1);
        List<Note> notes = noteMapper.selectList(noteWrapper);
        
        // 按原有顺序排序笔记（按点赞时间）
        Map<Long, Note> noteMap = notes.stream()
                .collect(Collectors.toMap(Note::getId, note -> note));
        
        List<NoteItemResponse> list = noteIds.stream()
                .map(noteMap::get)
                .filter(note -> note != null)
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, total, pageNum, pageSize);
    }

    @Override
    public PageResult<NoteItemResponse> getExploreNotes(Long categoryId, String sortBy, Integer pageNum, Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1);
        
        if (categoryId != null) {
            // 暂不支持按分类筛选
        }
        
        // 排序
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Note::getLikeCount);
        } else if ("new".equals(sortBy)) {
            wrapper.orderByDesc(Note::getCreatedAt);
        } else {
            wrapper.orderByDesc(Note::getLikeCount).orderByDesc(Note::getCreatedAt);
        }
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<NoteItemResponse> getNearbyNotes(Double latitude, Double longitude, Double distance, Integer pageNum, Integer pageSize) {
        // 简化实现，实际应使用地理位置查询
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1)
               .isNotNull(Note::getLatitude)
               .isNotNull(Note::getLongitude)
               .orderByDesc(Note::getCreatedAt);
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<NoteItemResponse> getFollowingNotes(Long userId, Integer pageNum, Integer pageSize) {
        // 获取关注的用户ID列表
        LambdaQueryWrapper<UserFollow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(UserFollow::getUserId, userId);
        List<UserFollow> follows = userFollowMapper.selectList(followWrapper);
        
        if (follows.isEmpty()) {
            return PageResult.empty(pageNum, pageSize);
        }
        
        List<Long> followingIds = follows.stream()
                .map(UserFollow::getFollowUserId)
                .collect(Collectors.toList());
        
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1)
               .in(Note::getUserId, followingIds)
               .orderByDesc(Note::getCreatedAt);
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    @Override
    public NoteDetailResponse getNoteDetail(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }
        
        // 查询作者信息
        User author = userMapper.selectById(note.getUserId());
        
        // 查询标签
        List<String> tags = getNoteTagNames(noteId);
        
        // 构建响应
        NoteDetailResponse response = new NoteDetailResponse();
        response.setId(note.getId());
        response.setImage(note.getCoverImage());
        response.setImages(parseImages(note.getImages()));
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setAuthor(author != null ? author.getUsername() : "未知用户");
        response.setAuthorAvatar(author != null ? author.getAvatar() : null);
        response.setAuthorId(note.getUserId());
        response.setPublishTime(TimeUtil.formatRelativeTime(note.getCreatedAt()));
        response.setTags(tags);
        response.setLikeCount(note.getLikeCount());
        response.setCommentCount(note.getCommentCount());
        response.setViewCount(note.getViewCount());
        response.setFavoriteCount(note.getFavoriteCount());
        response.setLocation(note.getLocation());
        response.setCreatedAt(note.getCreatedAt());
        
        // 查询关联商家
        if (note.getShopId() != null) {
            Shop shop = shopMapper.selectById(note.getShopId());
            if (shop != null) {
                response.setShopId(shop.getId());
                response.setShopName(shop.getName());
            }
        }
        
        // 查询用户互动状态
        if (userId != null) {
            boolean liked = isLiked(userId, noteId);
            boolean bookmarked = isBookmarked(userId, noteId);
            boolean following = isFollowing(userId, note.getUserId());
            
            log.info("笔记详情查询 - 用户ID: {}, 笔记ID: {}, 点赞: {}, 收藏: {}, 关注: {}", 
                    userId, noteId, liked, bookmarked, following);
            
            response.setIsLiked(liked);
            response.setIsBookmarked(bookmarked);
            response.setIsFollowing(following);
            response.setIsAuthor(userId.equals(note.getUserId()));
        } else {
            response.setIsLiked(false);
            response.setIsBookmarked(false);
            response.setIsFollowing(false);
            response.setIsAuthor(false);
        }
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishNote(Long userId, PublishNoteRequest request) {
        Note note = new Note();
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
                ? request.getImages().get(0) : null);
        note.setImages(String.join(",", request.getImages()));
        note.setShopId(request.getShopId());
        note.setLocation(request.getLocation());
        note.setLatitude(request.getLatitude());
        note.setLongitude(request.getLongitude());
        note.setStatus(request.getStatus() != null ? request.getStatus() : 1); // 1=公开，2=仅自己可见
        note.setLikeCount(0);
        note.setCommentCount(0);
        note.setViewCount(0);
        note.setFavoriteCount(0);
        
        noteMapper.insert(note);
        
        // 保存标签关联
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            saveNoteTags(note.getId(), request.getTags());
        }
        
        // 保存话题关联
        if (request.getTopics() != null && !request.getTopics().isEmpty()) {
            saveNoteTopics(note.getId(), request.getTopics());
        }
        
        // 更新用户笔记数
        userStatsMapper.incrementNoteCount(userId);
        
        log.info("用户{}发布笔记成功，笔记ID={}", userId, note.getId());
        
        return note.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNote(Long userId, Long noteId, PublishNoteRequest request) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BusinessException(40300, "无权限操作");
        }
        
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            note.setCoverImage(request.getImages().get(0));
            note.setImages(String.join(",", request.getImages()));
        }
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
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer likeNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }
        
        // 检查是否已点赞
        if (isLiked(userId, noteId)) {
            throw new BusinessException(40001, "已点赞");
        }
        
        // 插入点赞记录
        UserNoteLike like = new UserNoteLike();
        like.setUserId(userId);
        like.setNoteId(noteId);
        userNoteLikeMapper.insert(like);
        
        // 更新笔记点赞数
        noteMapper.incrementLikeCount(noteId);
        
        // 更新作者获赞数
        userStatsMapper.incrementLikeCount(note.getUserId());
        
        // 发送点赞通知
        if (!userId.equals(note.getUserId())) {
            User user = userMapper.selectById(userId);
            messageService.sendNotification(note.getUserId(), "收到点赞", 
                    user.getUsername() + " 赞了你的笔记", 1, noteId);
        }
        
        return note.getLikeCount() + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40402, "笔记不存在");
        }
        
        // 检查是否已点赞
        LambdaQueryWrapper<UserNoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoteLike::getUserId, userId)
               .eq(UserNoteLike::getNoteId, noteId);
        UserNoteLike like = userNoteLikeMapper.selectOne(wrapper);
        
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookmarkNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }
        
        // 检查是否已收藏
        if (isBookmarked(userId, noteId)) {
            throw new BusinessException(40001, "已收藏");
        }
        
        // 插入收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setType(1); // 笔记类型
        favorite.setTargetId(noteId);
        userFavoriteMapper.insert(favorite);
        
        // 更新笔记收藏数
        noteMapper.incrementFavoriteCount(noteId);
        
        // 更新用户收藏数
        userStatsMapper.incrementFavoriteCount(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbookmarkNote(Long userId, Long noteId) {
        // 检查是否已收藏
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getType, 1)
               .eq(UserFavorite::getTargetId, noteId);
        UserFavorite favorite = userFavoriteMapper.selectOne(wrapper);
        
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

    @Override
    @Async("asyncExecutor")
    public void increaseViewCount(Long noteId) {
        noteMapper.incrementViewCount(noteId);
    }

    @Override
    public boolean isLiked(Long userId, Long noteId) {
        if (userId == null) return false;
        LambdaQueryWrapper<UserNoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoteLike::getUserId, userId)
               .eq(UserNoteLike::getNoteId, noteId);
        long count = userNoteLikeMapper.selectCount(wrapper);
        boolean result = count > 0;
        log.debug("检查点赞状态 - 用户ID: {}, 笔记ID: {}, 记录数: {}, 结果: {}", userId, noteId, count, result);
        return result;
    }

    @Override
    public boolean isBookmarked(Long userId, Long noteId) {
        if (userId == null) return false;
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getType, 1)
               .eq(UserFavorite::getTargetId, noteId);
        long count = userFavoriteMapper.selectCount(wrapper);
        boolean result = count > 0;
        log.debug("检查收藏状态 - 用户ID: {}, 笔记ID: {}, 记录数: {}, 结果: {}", userId, noteId, count, result);
        return result;
    }

    @Override
    public PageResult<NoteItemResponse> searchNotes(String keyword, Integer pageNum, Integer pageSize) {
        Page<Note> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1)
               .and(w -> w.like(Note::getTitle, keyword)
                         .or()
                         .like(Note::getContent, keyword))
               .orderByDesc(Note::getCreatedAt);
        
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItem)
                .collect(Collectors.toList());
        
        return PageResult.of(list, notePage.getTotal(), pageNum, pageSize);
    }

    private NoteItemResponse convertToNoteItem(Note note) {
        NoteItemResponse item = new NoteItemResponse();
        item.setId(note.getId().toString());
        item.setImage(note.getCoverImage());
        item.setTitle(note.getTitle());
        item.setLikes(note.getLikeCount());
        item.setViews(note.getViewCount());
        item.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
        
        // 查询作者信息
        User author = userMapper.selectById(note.getUserId());
        if (author != null) {
            item.setAuthor(author.getUsername());
            item.setAuthorAvatar(author.getAvatar());
            item.setAuthorId(author.getId().toString());
        }
        
        // 计算热度标签
        if (note.getLikeCount() > 1000) {
            item.setTag("热门");
            item.setTagClass("tag-hot");
        } else if (note.getCreatedAt() != null && 
                   note.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
            item.setTag("新发");
            item.setTagClass("tag-new");
        }
        
        return item;
    }

    private List<String> getNoteTagNames(Long noteId) {
        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, noteId);
        List<NoteTag> noteTags = noteTagMapper.selectList(wrapper);
        
        return noteTags.stream()
                .map(NoteTag::getTagName)
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
            NoteTag noteTag = new NoteTag();
            noteTag.setNoteId(noteId);
            noteTag.setTagName(tagName);
            noteTagMapper.insert(noteTag);
        }
    }

    private void saveNoteTopics(Long noteId, List<Long> topics) {
        for (Long topicId : topics) {
            NoteTopic noteTopic = new NoteTopic();
            noteTopic.setNoteId(noteId);
            noteTopic.setTopicId(topicId);
            noteTopicMapper.insert(noteTopic);
        }
    }

    private void deleteNoteTags(Long noteId) {
        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, noteId);
        noteTagMapper.delete(wrapper);
    }
    
    /**
     * 检查是否关注
     */
    private boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }
        
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getUserId, userId)
               .eq(UserFollow::getFollowUserId, targetUserId);
        long count = userFollowMapper.selectCount(wrapper);
        boolean result = count > 0;
        log.debug("检查关注状态 - 用户ID: {}, 目标用户ID: {}, 记录数: {}, 结果: {}", userId, targetUserId, count, result);
        return result;
    }
}

package com.businessreviews.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.event.CommentCreatedEvent;
import com.businessreviews.model.dto.app.AddCommentDTO;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.enums.CommentStatus;
import com.businessreviews.enums.NoteStatus;
import com.businessreviews.service.app.CommentService;
import com.businessreviews.service.app.MessageService;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 * <p>
 * 处理笔记评论的增删改查及点赞互动。
 * 核心功能包括：
 * 1. 评论列表查询（支持按热度和时间排序）
 * 2. 评论回复管理（二级评论结构）
 * 3. 评论点赞与取消
 * 4. 评论发布与异步审核触发
 * <p>
 * 性能优化：使用 In-Memory Map Assembly 模式解决 N+1 查询问题。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentDO> implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 获取笔记的一级评论列表
     * <p>
     * 查询指定笔记下的所有一级评论（parentId为null）。
     * 排序规则：优先按点赞数降序，其次按创建时间降序。
     * </p>
     *
     * @param noteId   笔记ID
     * @param userId   当前登录用户ID（用于判断点赞状态）
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论VO分页列表
     */
    @Override
    public PageResult<CommentVO> getNoteComments(Long noteId, Long userId, Integer pageNum, Integer pageSize) {
        Page<CommentDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getNoteId, noteId)
                .isNull(CommentDO::getParentId)
                .eq(CommentDO::getStatus, 1)
                .orderByDesc(CommentDO::getLikeCount)
                .orderByDesc(CommentDO::getCreatedAt);

        Page<CommentDO> commentPage = commentMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<CommentVO> list = convertCommentList(commentPage.getRecords(), userId, true);

        return PageResult.of(list, commentPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取评论的回复列表（二级评论）
     * <p>
     * 查询指定评论下的所有回复。
     * 排序规则：按创建时间升序排列（楼层顺序）。
     * </p>
     *
     * @param commentId 父评论ID
     * @param userId    当前登录用户ID
     * @param pageNum   页码
     * @param pageSize  每页数量
     * @return 回复VO分页列表
     */
    @Override
    public PageResult<CommentVO> getCommentReplies(Long commentId, Long userId, Integer pageNum, Integer pageSize) {
        Page<CommentDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getParentId, commentId)
                .eq(CommentDO::getStatus, 1)
                .orderByAsc(CommentDO::getCreatedAt);

        Page<CommentDO> replyPage = commentMapper.selectPage(page, wrapper);

        // 使用批量转换，解决N+1查询问题
        List<CommentVO> list = convertCommentList(replyPage.getRecords(), userId, false);

        return PageResult.of(list, replyPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 发布评论或回复
     * <p>
     * 支持发布一级评论或回复已有评论。
     * 发布成功后会：
     * 1. 增加笔记评论数
     * 2. 发送系统通知给被评论者
     * 3. 触发异步AI内容审核事件
     * </p>
     *
     * @param userId  当前用户ID
     * @param request 评论参数（包含内容、笔记ID、父评论ID等）
     * @return 新创建的评论VO
     * @throws BusinessException 如果笔记或父评论不存在(40402)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(Long userId, AddCommentDTO request) {
        // 检查笔记是否存在
        NoteDO note = noteMapper.selectById(request.getNoteId());
        if (note == null || note.getStatus() != NoteStatus.NORMAL.getCode()) {
            throw new BusinessException(40402, "笔记不存在");
        }

        CommentDO comment = new CommentDO();
        comment.setNoteId(Long.parseLong(request.getNoteId()));
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setStatus(CommentStatus.NORMAL.getCode());
        comment.setLikeCount(0);
        comment.setReplyCount(0);

        // 如果是回复评论
        if (request.getParentId() != null) {
            CommentDO parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException(40402, "评论不存在");
            }
            comment.setParentId(Long.parseLong(request.getParentId()));

            // 更新父评论的回复数
            commentMapper.incrementReplyCount(parentComment.getId());
        }

        commentMapper.insert(comment);

        // 更新笔记评论数
        noteMapper.incrementCommentCount(note.getId());

        // 发送评论通知
        if (!userId.equals(note.getUserId())) {
            UserDO user = userMapper.selectById(userId);
            // 使用新的系统通知方法，包含发送者信息和笔记图片
            messageService.sendSystemNotice(note.getUserId(), userId, 2, note.getId(),
                    user.getUsername() + " 评论了你的笔记", note.getCoverImage());
        }

        // 发布评论创建事件，触发异步AI内容审核
        eventPublisher.publishEvent(new CommentCreatedEvent(
                this,
                comment.getId(),
                comment.getContent(),
                userId,
                note.getId()));
        log.info("用户{}发布评论成功，评论id={}，已触发异步审核", userId, comment.getId());

        return convertToResponse(comment, userId, false);
    }

    /**
     * 删除评论
     * <p>
     * 软删除评论（status设为0）。
     * 同时会减少笔记评论数，如果是回复还会减少父评论的回复数。
     * </p>
     *
     * @param userId    当前用户ID
     * @param commentId 待删除的评论ID
     * @throws BusinessException 如果评论不存在(40402)或无权限操作(40300)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        CommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(40402, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(40300, "无权限操作");
        }

        // 软删除
        comment.setStatus(CommentStatus.DELETED.getCode());
        commentMapper.updateById(comment);

        // 更新笔记评论数
        noteMapper.decrementCommentCount(comment.getNoteId());

        // 如果是回复，更新父评论的回复数
        if (comment.getParentId() != null) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
    }

    /**
     * 点赞评论
     *
     * @param userId    当前用户ID
     * @param commentId 评论ID
     * @return 点赞后的最新点赞数
     * @throws BusinessException 如果评论不存在(40402)或已点赞(40001)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer likeComment(Long userId, Long commentId) {
        CommentDO comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 1) {
            throw new BusinessException(40402, "评论不存在");
        }

        // 检查是否已点赞
        if (isCommentLiked(userId, commentId)) {
            throw new BusinessException(40001, "已点赞");
        }

        // 插入点赞记录
        CommentLikeDO like = new CommentLikeDO();
        like.setUserId(userId);
        like.setCommentId(commentId);
        commentLikeMapper.insert(like);

        // 更新评论点赞数
        commentMapper.incrementLikeCount(commentId);

        return comment.getLikeCount() + 1;
    }

    /**
     * 取消点赞评论
     *
     * @param userId    当前用户ID
     * @param commentId 评论ID
     * @return 取消点赞后的最新点赞数
     * @throws BusinessException 如果评论不存在(40402)或未点赞(40001)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeComment(Long userId, Long commentId) {
        CommentDO comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(40402, "评论不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<CommentLikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLikeDO::getUserId, userId)
                .eq(CommentLikeDO::getCommentId, commentId);
        CommentLikeDO like = commentLikeMapper.selectOne(wrapper);

        if (like == null) {
            throw new BusinessException(40001, "未点赞");
        }

        // 删除点赞记录
        commentLikeMapper.deleteById(like.getId());

        // 更新评论点赞数
        commentMapper.decrementLikeCount(commentId);

        return Math.max(0, comment.getLikeCount() - 1);
    }

    /**
     * 检查是否已点赞
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     * @return true=已点赞, false=未点赞
     */
    @Override
    public boolean isCommentLiked(Long userId, Long commentId) {
        if (userId == null)
            return false;
        LambdaQueryWrapper<CommentLikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLikeDO::getUserId, userId)
                .eq(CommentLikeDO::getCommentId, commentId);
        return commentLikeMapper.selectCount(wrapper) > 0;
    }

    /**
     * 批量转换评论列表 - 解决N+1查询问题
     * 使用In-Memory Map预加载用户信息和回复数据，将查询复杂度从O(N)降为O(1)
     * 
     * 优化策略：
     * 1. 一次性查询所有父评论的回复（使用 IN 条件），避免循环内查询
     * 2. 批量查询所有涉及的用户信息
     * 3. 将数据组装成 Map，在内存中进行 O(1) 查找
     * 
     * @param comments       评论列表
     * @param userId         当前用户ID
     * @param includeReplies 是否包含回复
     * @return 转换后的VO列表
     */
    private List<CommentVO> convertCommentList(List<CommentDO> comments, Long userId, boolean includeReplies) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 收集所有主评论的ID和用户ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> commentIdsWithReplies = new HashSet<>();

        for (CommentDO comment : comments) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            // 收集有回复的评论ID，用于后续批量查询
            if (includeReplies && comment.getReplyCount() != null && comment.getReplyCount() > 0) {
                commentIdsWithReplies.add(comment.getId());
            }
        }

        // 2. 批量查询所有回复（核心优化：一次 SQL 替代 N 次循环查询）
        List<CommentDO> allReplies = new ArrayList<>();
        if (!commentIdsWithReplies.isEmpty()) {
            // 使用 IN 条件一次性查询所有父评论的回复
            // 注意：这里查询每个父评论的所有回复，然后在内存中限制显示数量
            LambdaQueryWrapper<CommentDO> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.in(CommentDO::getParentId, commentIdsWithReplies)
                    .eq(CommentDO::getStatus, 1)
                    .orderByAsc(CommentDO::getCreatedAt);
            allReplies = commentMapper.selectList(replyWrapper);

            // 收集回复的用户ID
            for (CommentDO reply : allReplies) {
                if (reply.getUserId() != null) {
                    userIds.add(reply.getUserId());
                }
            }
        }

        // 3. 批量查询用户信息（一次 SQL 查询所有用户）
        Map<Long, UserDO> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        }

        // 4. 将回复按父评论ID分组，并限制每个父评论只显示前3条回复
        Map<Long, List<CommentDO>> replyMap = allReplies.stream()
                .collect(Collectors.groupingBy(CommentDO::getParentId))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .limit(3) // 限制每个父评论显示3条回复
                                .collect(Collectors.toList())));

        // 5. 转换评论
        final Map<Long, UserDO> finalUserMap = userMap;
        return comments.stream()
                .map(comment -> convertToResponse(comment, userId, includeReplies, finalUserMap, replyMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换单个评论 - 简化版本，用于新创建的评论
     * 
     * @param comment        评论实体
     * @param userId         当前用户ID
     * @param includeReplies 是否包含回复
     * @return 评论VO
     */
    private CommentVO convertToResponse(CommentDO comment, Long userId, boolean includeReplies) {
        // 获取评论者信息
        UserDO user = userMapper.selectById(comment.getUserId());
        Map<Long, UserDO> userMap = user != null
                ? Collections.singletonMap(user.getId(), user)
                : Collections.emptyMap();

        // 新创建的评论没有回复
        Map<Long, List<CommentDO>> replyMap = Collections.emptyMap();

        return convertToResponse(comment, userId, includeReplies, userMap, replyMap);
    }

    /**
     * 转换单个评论 - 使用预加载的Map获取关联数据
     * 
     * @param comment        评论实体
     * @param userId         当前用户ID
     * @param includeReplies 是否包含回复
     * @param userMap        预加载的用户Map
     * @param replyMap       预加载的回复Map
     * @return 评论VO
     */
    private CommentVO convertToResponse(CommentDO comment, Long userId, boolean includeReplies,
            Map<Long, UserDO> userMap, Map<Long, List<CommentDO>> replyMap) {
        CommentVO response = new CommentVO();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setLikes(comment.getLikeCount());
        response.setReplyCount(comment.getReplyCount());
        response.setTime(comment.getCreatedAt());
        response.setTimeAgo(TimeUtil.formatRelativeTime(comment.getCreatedAt()));

        // 从预加载的Map获取评论者信息，O(1)复杂度
        UserDO user = userMap.get(comment.getUserId());
        if (user != null) {
            response.setAuthorId(user.getId());
            response.setAuthor(user.getUsername());
            response.setAvatar(user.getAvatar());
        }

        // 检查是否已点赞
        response.setLiked(isCommentLiked(userId, comment.getId()));

        // 从预加载的Map获取回复
        if (includeReplies && comment.getReplyCount() > 0) {
            List<CommentDO> replies = replyMap.getOrDefault(comment.getId(), Collections.emptyList());
            List<CommentVO> replyList = replies.stream()
                    .map(reply -> {
                        CommentVO replyVO = new CommentVO();
                        replyVO.setId(reply.getId());
                        replyVO.setContent(reply.getContent());
                        replyVO.setLikes(reply.getLikeCount());
                        replyVO.setReplyCount(reply.getReplyCount());
                        replyVO.setTime(reply.getCreatedAt());
                        replyVO.setTimeAgo(TimeUtil.formatRelativeTime(reply.getCreatedAt()));

                        UserDO replyUser = userMap.get(reply.getUserId());
                        if (replyUser != null) {
                            replyVO.setAuthorId(replyUser.getId());
                            replyVO.setAuthor(replyUser.getUsername());
                            replyVO.setAvatar(replyUser.getAvatar());
                        }

                        replyVO.setLiked(isCommentLiked(userId, reply.getId()));
                        replyVO.setReplies(new ArrayList<>());
                        return replyVO;
                    })
                    .collect(Collectors.toList());
            response.setReplies(replyList);
        } else {
            response.setReplies(new ArrayList<>());
        }

        return response;
    }
}

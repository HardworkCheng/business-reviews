package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.AddCommentRequest;
import com.businessreviews.dto.response.CommentResponse;
import com.businessreviews.entity.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.CommentService;
import com.businessreviews.service.MessageService;
import com.businessreviews.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final MessageService messageService;

    @Override
    public PageResult<CommentResponse> getNoteComments(Long noteId, Long userId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getNoteId, noteId)
               .isNull(Comment::getParentId)
               .eq(Comment::getStatus, 1)
               .orderByDesc(Comment::getLikeCount)
               .orderByDesc(Comment::getCreatedAt);
        
        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);
        
        List<CommentResponse> list = commentPage.getRecords().stream()
                .map(comment -> convertToResponse(comment, userId, true))
                .collect(Collectors.toList());
        
        return PageResult.of(list, commentPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<CommentResponse> getCommentReplies(Long commentId, Long userId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId)
               .eq(Comment::getStatus, 1)
               .orderByAsc(Comment::getCreatedAt);
        
        Page<Comment> replyPage = commentMapper.selectPage(page, wrapper);
        
        List<CommentResponse> list = replyPage.getRecords().stream()
                .map(comment -> convertToResponse(comment, userId, false))
                .collect(Collectors.toList());
        
        return PageResult.of(list, replyPage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse addComment(Long userId, AddCommentRequest request) {
        // 检查笔记是否存在
        Note note = noteMapper.selectById(request.getNoteId());
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }
        
        Comment comment = new Comment();
        comment.setNoteId(Long.parseLong(request.getNoteId()));
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        
        // 如果是回复评论
        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
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
            User user = userMapper.selectById(userId);
            // 使用新的系统通知方法，包含发送者信息和笔记图片
            messageService.sendSystemNotice(note.getUserId(), userId, 2, note.getId(),
                    user.getUsername() + " 评论了你的笔记", note.getCoverImage());
        }
        
        return convertToResponse(comment, userId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(40402, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(40300, "无权限操作");
        }
        
        // 软删除
        comment.setStatus(0);
        commentMapper.updateById(comment);
        
        // 更新笔记评论数
        noteMapper.decrementCommentCount(comment.getNoteId());
        
        // 如果是回复，更新父评论的回复数
        if (comment.getParentId() != null) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer likeComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getStatus() != 1) {
            throw new BusinessException(40402, "评论不存在");
        }
        
        // 检查是否已点赞
        if (isCommentLiked(userId, commentId)) {
            throw new BusinessException(40001, "已点赞");
        }
        
        // 插入点赞记录
        CommentLike like = new CommentLike();
        like.setUserId(userId);
        like.setCommentId(commentId);
        commentLikeMapper.insert(like);
        
        // 更新评论点赞数
        commentMapper.incrementLikeCount(commentId);
        
        return comment.getLikeCount() + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer unlikeComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(40402, "评论不存在");
        }
        
        // 检查是否已点赞
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getUserId, userId)
               .eq(CommentLike::getCommentId, commentId);
        CommentLike like = commentLikeMapper.selectOne(wrapper);
        
        if (like == null) {
            throw new BusinessException(40001, "未点赞");
        }
        
        // 删除点赞记录
        commentLikeMapper.deleteById(like.getId());
        
        // 更新评论点赞数
        commentMapper.decrementLikeCount(commentId);
        
        return Math.max(0, comment.getLikeCount() - 1);
    }

    @Override
    public boolean isCommentLiked(Long userId, Long commentId) {
        if (userId == null) return false;
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getUserId, userId)
               .eq(CommentLike::getCommentId, commentId);
        return commentLikeMapper.selectCount(wrapper) > 0;
    }

    private CommentResponse convertToResponse(Comment comment, Long userId, boolean includeReplies) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setLikes(comment.getLikeCount());
        response.setReplyCount(comment.getReplyCount());
        response.setTime(comment.getCreatedAt());
        response.setTimeAgo(TimeUtil.formatRelativeTime(comment.getCreatedAt()));
        
        // 查询评论者信息
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setAuthorId(user.getId());
            response.setAuthor(user.getUsername());
            response.setAvatar(user.getAvatar());
        }
        
        // 检查是否已点赞
        response.setLiked(isCommentLiked(userId, comment.getId()));
        
        // 查询前3条回复
        if (includeReplies && comment.getReplyCount() > 0) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getParentId, comment.getId())
                   .eq(Comment::getStatus, 1)
                   .orderByAsc(Comment::getCreatedAt)
                   .last("LIMIT 3");
            List<Comment> replies = commentMapper.selectList(wrapper);
            
            List<CommentResponse> replyList = replies.stream()
                    .map(reply -> convertToResponse(reply, userId, false))
                    .collect(Collectors.toList());
            response.setReplies(replyList);
        } else {
            response.setReplies(new ArrayList<>());
        }
        
        return response;
    }
}

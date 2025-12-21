package com.businessreviews.service.impl.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.app.AddCommentDTO;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.app.CommentService;
import com.businessreviews.service.app.MessageService;
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
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentDO> implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final MessageService messageService;

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
        
        List<CommentVO> list = commentPage.getRecords().stream()
                .map(comment -> convertToResponse(comment, userId, true))
                .collect(Collectors.toList());
        
        return PageResult.of(list, commentPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<CommentVO> getCommentReplies(Long commentId, Long userId, Integer pageNum, Integer pageSize) {
        Page<CommentDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentDO::getParentId, commentId)
               .eq(CommentDO::getStatus, 1)
               .orderByAsc(CommentDO::getCreatedAt);
        
        Page<CommentDO> replyPage = commentMapper.selectPage(page, wrapper);
        
        List<CommentVO> list = replyPage.getRecords().stream()
                .map(comment -> convertToResponse(comment, userId, false))
                .collect(Collectors.toList());
        
        return PageResult.of(list, replyPage.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(Long userId, AddCommentDTO request) {
        // 检查笔记是否存在
        NoteDO note = noteMapper.selectById(request.getNoteId());
        if (note == null || note.getStatus() != 1) {
            throw new BusinessException(40402, "笔记不存在");
        }
        
        CommentDO comment = new CommentDO();
        comment.setNoteId(Long.parseLong(request.getNoteId()));
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setStatus(1);
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
        
        return convertToResponse(comment, userId, false);
    }

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

    @Override
    public boolean isCommentLiked(Long userId, Long commentId) {
        if (userId == null) return false;
        LambdaQueryWrapper<CommentLikeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLikeDO::getUserId, userId)
               .eq(CommentLikeDO::getCommentId, commentId);
        return commentLikeMapper.selectCount(wrapper) > 0;
    }

    private CommentVO convertToResponse(CommentDO comment, Long userId, boolean includeReplies) {
        CommentVO response = new CommentVO();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setLikes(comment.getLikeCount());
        response.setReplyCount(comment.getReplyCount());
        response.setTime(comment.getCreatedAt());
        response.setTimeAgo(TimeUtil.formatRelativeTime(comment.getCreatedAt()));
        
        // 查询评论者信息
        UserDO user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            response.setAuthorId(user.getId());
            response.setAuthor(user.getUsername());
            response.setAvatar(user.getAvatar());
        }
        
        // 检查是否已点赞
        response.setLiked(isCommentLiked(userId, comment.getId()));
        
        // 查询前3条回复
        if (includeReplies && comment.getReplyCount() > 0) {
            LambdaQueryWrapper<CommentDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommentDO::getParentId, comment.getId())
                   .eq(CommentDO::getStatus, 1)
                   .orderByAsc(CommentDO::getCreatedAt)
                   .last("LIMIT 3");
            List<CommentDO> replies = commentMapper.selectList(wrapper);
            
            List<CommentVO> replyList = replies.stream()
                    .map(reply -> convertToResponse(reply, userId, false))
                    .collect(Collectors.toList());
            response.setReplies(replyList);
        } else {
            response.setReplies(new ArrayList<>());
        }
        
        return response;
    }
}

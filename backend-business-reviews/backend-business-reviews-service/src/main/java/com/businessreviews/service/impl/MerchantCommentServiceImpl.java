package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.CommentResponse;
import com.businessreviews.entity.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.MerchantCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCommentServiceImpl implements MerchantCommentService {

    private final NoteCommentMapper noteCommentMapper;
    private final NoteMapper noteMapper;
    private final ShopMapper shopMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<CommentResponse> getCommentList(Long merchantId, Integer pageNum, Integer pageSize,
            Integer status, String keyword) {
        log.info("获取评论列表: merchantId={}, pageNum={}, pageSize={}", merchantId, pageNum, pageSize);
        
        validateMerchant(merchantId);
        
        // 获取商家关联的笔记ID列表
        List<Long> noteIds = getNoteIdsByMerchant(merchantId);
        if (noteIds.isEmpty()) {
            return emptyPageResult(pageNum, pageSize);
        }
        
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteComment::getNoteId, noteIds);
        
        if (status != null) {
            wrapper.eq(NoteComment::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(NoteComment::getContent, keyword);
        }
        wrapper.orderByDesc(NoteComment::getCreatedAt);
        
        Page<NoteComment> page = new Page<>(pageNum, pageSize);
        Page<NoteComment> commentPage = noteCommentMapper.selectPage(page, wrapper);
        
        List<CommentResponse> list = commentPage.getRecords().stream()
                .map(this::convertToCommentResponse)
                .collect(Collectors.toList());
        
        PageResult<CommentResponse> result = new PageResult<>();
        result.setList(list);
        result.setTotal(commentPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    @Transactional
    public void replyComment(Long merchantId, Long operatorId, Long commentId, String content) {
        log.info("回复评论: merchantId={}, commentId={}", merchantId, commentId);
        
        validateMerchant(merchantId);
        
        NoteComment parentComment = noteCommentMapper.selectById(commentId);
        if (parentComment == null) {
            throw new BusinessException(40404, "评论不存在");
        }
        
        // 创建回复评论
        NoteComment reply = new NoteComment();
        reply.setNoteId(parentComment.getNoteId());
        reply.setUserId(operatorId);
        reply.setParentId(commentId);
        reply.setContent(content);
        reply.setLikeCount(0);
        reply.setStatus(1);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        
        noteCommentMapper.insert(reply);
        
        // 更新笔记评论数
        noteMapper.incrementCommentCount(parentComment.getNoteId());
        
        log.info("评论回复成功: replyId={}", reply.getId());
    }

    @Override
    @Transactional
    public void deleteComment(Long merchantId, Long operatorId, Long commentId) {
        log.info("删除评论: merchantId={}, commentId={}", merchantId, commentId);
        
        validateMerchant(merchantId);
        
        NoteComment comment = noteCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(40404, "评论不存在");
        }
        
        // 软删除：更新状态为隐藏
        comment.setStatus(2);
        comment.setUpdatedAt(LocalDateTime.now());
        noteCommentMapper.updateById(comment);
        
        // 更新笔记评论数
        noteMapper.decrementCommentCount(comment.getNoteId());
        
        log.info("评论删除成功: commentId={}", commentId);
    }

    @Override
    public Map<String, Object> getCommentStats(Long merchantId) {
        log.info("获取评论统计: merchantId={}", merchantId);
        
        validateMerchant(merchantId);
        
        List<Long> noteIds = getNoteIdsByMerchant(merchantId);
        
        long totalComments = 0;
        long pendingComments = 0;
        
        if (!noteIds.isEmpty()) {
            LambdaQueryWrapper<NoteComment> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.in(NoteComment::getNoteId, noteIds);
            totalComments = noteCommentMapper.selectCount(totalWrapper);
            
            LambdaQueryWrapper<NoteComment> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.in(NoteComment::getNoteId, noteIds)
                    .eq(NoteComment::getStatus, 1);
            pendingComments = noteCommentMapper.selectCount(pendingWrapper);
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComments", totalComments);
        stats.put("pendingComments", pendingComments);
        stats.put("repliedComments", totalComments - pendingComments);
        
        return stats;
    }
    
    private void validateMerchant(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
        if (merchant.getStatus() != 1) {
            throw new BusinessException(40300, "商家账号已被禁用");
        }
    }
    
    private List<Long> getNoteIdsByMerchant(Long merchantId) {
        // 获取商家关联的门店
        List<Long> shopIds = getShopIdsByMerchant(merchantId);
        if (shopIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取门店关联的笔记
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Note::getShopId, shopIds);
        List<Note> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(Note::getId).collect(Collectors.toList());
    }
    
    private List<Long> getShopIdsByMerchant(Long merchantId) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(Shop::getMerchantId, merchantId);
        List<Shop> shops = shopMapper.selectList(wrapper);
        return shops.stream().map(Shop::getId).collect(Collectors.toList());
    }
    
    private PageResult<CommentResponse> emptyPageResult(Integer pageNum, Integer pageSize) {
        PageResult<CommentResponse> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
    
    private CommentResponse convertToCommentResponse(NoteComment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setLikes(comment.getLikeCount());
        response.setLiked(false);
        response.setTime(comment.getCreatedAt());
        response.setReplyCount(0);
        
        // 获取用户信息
        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                response.setAuthorId(user.getId());
                response.setAuthor(user.getUsername());
                response.setAvatar(user.getAvatar());
            }
        }
        
        return response;
    }
}

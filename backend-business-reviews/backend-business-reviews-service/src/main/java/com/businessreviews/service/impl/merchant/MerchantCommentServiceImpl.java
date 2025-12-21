package com.businessreviews.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.vo.CommentVO;
import com.businessreviews.model.dataobject.*;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.*;
import com.businessreviews.service.merchant.MerchantCommentService;
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
    public PageResult<CommentVO> getCommentList(Long merchantId, Integer pageNum, Integer pageSize,
            Integer status, String keyword) {
        log.info("获取评论列表: merchantId={}, pageNum={}, pageSize={}", merchantId, pageNum, pageSize);
        
        validateMerchant(merchantId);
        
        // 获取商家关联的笔记ID列表
        List<Long> noteIds = getNoteIdsByMerchant(merchantId);
        if (noteIds.isEmpty()) {
            return emptyPageResult(pageNum, pageSize);
        }
        
        LambdaQueryWrapper<NoteCommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteCommentDO::getNoteId, noteIds);
        
        if (status != null) {
            wrapper.eq(NoteCommentDO::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(NoteCommentDO::getContent, keyword);
        }
        wrapper.orderByDesc(NoteCommentDO::getCreatedAt);
        
        Page<NoteCommentDO> page = new Page<>(pageNum, pageSize);
        Page<NoteCommentDO> commentPage = noteCommentMapper.selectPage(page, wrapper);
        
        List<CommentVO> list = commentPage.getRecords().stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());
        
        PageResult<CommentVO> result = new PageResult<>();
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
        
        NoteCommentDO parentComment = noteCommentMapper.selectById(commentId);
        if (parentComment == null) {
            throw new BusinessException(40404, "评论不存在");
        }
        
        // 创建回复评论
        NoteCommentDO reply = new NoteCommentDO();
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
        
        NoteCommentDO comment = noteCommentMapper.selectById(commentId);
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
            LambdaQueryWrapper<NoteCommentDO> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.in(NoteCommentDO::getNoteId, noteIds);
            totalComments = noteCommentMapper.selectCount(totalWrapper);
            
            LambdaQueryWrapper<NoteCommentDO> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.in(NoteCommentDO::getNoteId, noteIds)
                    .eq(NoteCommentDO::getStatus, 1);
            pendingComments = noteCommentMapper.selectCount(pendingWrapper);
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalComments", totalComments);
        stats.put("pendingComments", pendingComments);
        stats.put("repliedComments", totalComments - pendingComments);
        
        return stats;
    }
    
    private void validateMerchant(Long merchantId) {
        MerchantDO merchant = merchantMapper.selectById(merchantId);
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
        LambdaQueryWrapper<NoteDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NoteDO::getShopId, shopIds);
        List<NoteDO> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(NoteDO::getId).collect(Collectors.toList());
    }
    
    private List<Long> getShopIdsByMerchant(Long merchantId) {
        LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(ShopDO::getMerchantId, merchantId);
        List<ShopDO> shops = shopMapper.selectList(wrapper);
        return shops.stream().map(ShopDO::getId).collect(Collectors.toList());
    }
    
    private PageResult<CommentVO> emptyPageResult(Integer pageNum, Integer pageSize) {
        PageResult<CommentVO> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
    
    private CommentVO convertToCommentVO(NoteCommentDO comment) {
        CommentVO response = new CommentVO();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setLikes(comment.getLikeCount());
        response.setLiked(false);
        response.setTime(comment.getCreatedAt());
        response.setReplyCount(0);
        
        // 获取用户信息
        if (comment.getUserId() != null) {
            UserDO user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                response.setAuthorId(user.getId());
                response.setAuthor(user.getUsername());
                response.setAvatar(user.getAvatar());
            }
        }
        
        return response;
    }
}

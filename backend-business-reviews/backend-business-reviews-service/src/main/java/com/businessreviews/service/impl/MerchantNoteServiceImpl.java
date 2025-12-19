package com.businessreviews.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.dto.response.NoteDetailResponse;
import com.businessreviews.dto.response.NoteItemResponse;
import com.businessreviews.entity.Merchant;
import com.businessreviews.entity.Note;
import com.businessreviews.entity.NoteComment;
import com.businessreviews.entity.Shop;
import com.businessreviews.entity.User;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.NoteCommentMapper;
import com.businessreviews.mapper.NoteMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.service.MerchantNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家笔记服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantNoteServiceImpl implements MerchantNoteService {

    private final NoteMapper noteMapper;
    private final NoteCommentMapper noteCommentMapper;
    private final ShopMapper shopMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<NoteItemResponse> getNoteList(Long merchantId, Integer pageNum, Integer pageSize,
            Integer status, Long shopId, String keyword) {
        log.info("获取笔记列表: merchantId={}, pageNum={}, pageSize={}", merchantId, pageNum, pageSize);
        
        validateMerchant(merchantId);
        
        // 获取商家关联的用户ID（通过手机号匹配）
        Long merchantUserId = findUserIdByMerchantId(merchantId);
        log.info("商家关联的用户ID: merchantId={}, userId={}", merchantId, merchantUserId);
        
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        // 查询商家笔记：merchantId匹配 或者 (noteType=2 且 userId匹配)
        wrapper.and(w -> {
            w.eq(Note::getMerchantId, merchantId);
            if (merchantUserId != null) {
                // 也查询通过UniApp发布的商家笔记（noteType=2且userId匹配）
                w.or(inner -> inner.eq(Note::getNoteType, 2).eq(Note::getUserId, merchantUserId));
            }
        });
        
        if (status != null) {
            wrapper.eq(Note::getStatus, status);
        }
        if (shopId != null) {
            wrapper.eq(Note::getShopId, shopId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Note::getTitle, keyword);
        }
        wrapper.orderByDesc(Note::getCreatedAt);

        
        Page<Note> page = new Page<>(pageNum, pageSize);
        Page<Note> notePage = noteMapper.selectPage(page, wrapper);
        
        List<NoteItemResponse> list = notePage.getRecords().stream()
                .map(this::convertToNoteItemResponse)
                .collect(Collectors.toList());
        
        PageResult<NoteItemResponse> result = new PageResult<>();
        result.setList(list);
        result.setTotal(notePage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public NoteDetailResponse getNoteDetail(Long merchantId, Long noteId) {
        log.info("获取笔记详情: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        return convertToNoteDetailResponse(note);
    }

    @Override
    @Transactional
    public Long createNote(Long merchantId, Long operatorId, Map<String, Object> request) {
        log.info("创建笔记: merchantId={}, operatorId={}", merchantId, operatorId);
        
        validateMerchant(merchantId);
        
        // 确保商家用户在users表中有对应的账号
        Long userId = ensureMerchantUserExists(merchantId, operatorId);
        
        Note note = new Note();
        note.setTitle((String) request.get("title"));
        note.setContent((String) request.get("content"));
        
        if (request.get("shopId") != null) {
            note.setShopId(Long.valueOf(request.get("shopId").toString()));
        }
        
        // 处理图片
        if (request.get("images") != null) {
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) request.get("images");
            if (!images.isEmpty()) {
                note.setImages(String.join(",", images));
                note.setCoverImage(images.get(0));
            }
        }
        
        // 处理位置信息
        if (request.get("location") != null) {
            note.setLocation((String) request.get("location"));
        }
        if (request.get("latitude") != null) {
            note.setLatitude(new java.math.BigDecimal(request.get("latitude").toString()));
        }
        if (request.get("longitude") != null) {
            note.setLongitude(new java.math.BigDecimal(request.get("longitude").toString()));
        }
        
        // 设置商家笔记特有字段
        note.setUserId(userId);  // 使用确保存在的用户ID
        note.setNoteType(2);  // 标记为商家笔记
        note.setMerchantId(merchantId);  // 设置商家ID
        
        // 设置状态和推荐
        note.setStatus(request.get("status") != null ? (Integer) request.get("status") : 1);
        note.setIsRecommend(request.get("isRecommend") != null ? (Integer) request.get("isRecommend") : 0);
        note.setSyncStatus(1);  // 默认已同步
        
        // 初始化统计数据
        note.setLikeCount(0);
        note.setCommentCount(0);
        note.setViewCount(0);
        note.setFavoriteCount(0);
        note.setShareCount(0);
        
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        
        noteMapper.insert(note);
        log.info("笔记创建成功: noteId={}, userId={}", note.getId(), userId);
        
        return note.getId();
    }
    
    /**
     * 确保商家用户在users表中存在
     * 如果不存在，则创建一个对应的用户账号
     */
    private Long ensureMerchantUserExists(Long merchantId, Long operatorId) {
        // 先检查users表中是否已存在该ID的用户
        User existingUser = userMapper.selectById(operatorId);
        if (existingUser != null) {
            log.info("商家用户已存在于users表: userId={}", operatorId);
            return operatorId;
        }
        
        // 如果不存在，查询商家信息并创建用户
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
        
        // 创建用户账号
        User user = new User();
        user.setId(operatorId);  // 使用相同的ID
        user.setUsername(merchant.getName() + "官方");  // 使用商家名称作为用户名
        
        // 检查电话号码是否已被使用，如果已被使用则生成一个唯一的电话号码
        String phone = merchant.getContactPhone();
        if (phone != null && !phone.isEmpty()) {
            // 检查电话号码是否已存在
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, phone);
            Long phoneCount = userMapper.selectCount(phoneWrapper);
            
            if (phoneCount > 0) {
                // 电话号码已存在，生成一个唯一的电话号码（添加商家ID后缀）
                phone = phone + "_" + merchantId;
                log.info("原电话号码已存在，使用新电话号码: {}", phone);
            }
        }
        
        user.setPhone(phone);
        user.setAvatar(merchant.getLogo() != null ? merchant.getLogo() : "https://via.placeholder.com/100");  // 使用商家Logo或默认头像
        user.setBio(merchant.getName() + "的官方账号");
        user.setGender(0);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("为商家创建用户账号: userId={}, username={}, phone={}", operatorId, user.getUsername(), phone);
        
        return operatorId;
    }

    @Override
    @Transactional
    public void updateNote(Long merchantId, Long operatorId, Long noteId, Map<String, Object> request) {
        log.info("更新笔记: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        if (request.get("title") != null) {
            note.setTitle((String) request.get("title"));
        }
        if (request.get("content") != null) {
            note.setContent((String) request.get("content"));
        }
        if (request.get("shopId") != null) {
            note.setShopId(Long.valueOf(request.get("shopId").toString()));
        }
        if (request.get("images") != null) {
            @SuppressWarnings("unchecked")
            List<String> images = (List<String>) request.get("images");
            if (!images.isEmpty()) {
                note.setImages(String.join(",", images));
                note.setCoverImage(images.get(0));
            }
        }
        
        // 更新位置信息
        if (request.get("location") != null) {
            note.setLocation((String) request.get("location"));
        }
        if (request.get("latitude") != null) {
            note.setLatitude(new java.math.BigDecimal(request.get("latitude").toString()));
        }
        if (request.get("longitude") != null) {
            note.setLongitude(new java.math.BigDecimal(request.get("longitude").toString()));
        }
        if (request.get("isRecommend") != null) {
            note.setIsRecommend((Integer) request.get("isRecommend"));
        }
        if (request.get("status") != null) {
            note.setStatus((Integer) request.get("status"));
        }
        
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        log.info("笔记更新成功: noteId={}", noteId);
    }

    @Override
    @Transactional
    public void publishNote(Long merchantId, Long operatorId, Long noteId) {
        log.info("发布笔记: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        note.setStatus(1); // 正常/已发布
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        log.info("笔记发布成功: noteId={}", noteId);
    }

    @Override
    @Transactional
    public void offlineNote(Long merchantId, Long operatorId, Long noteId) {
        log.info("下线笔记: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        note.setStatus(2); // 隐藏/已下线
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        log.info("笔记下线成功: noteId={}", noteId);
    }

    @Override
    @Transactional
    public void deleteNote(Long merchantId, Long operatorId, Long noteId) {
        log.info("删除笔记: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        noteMapper.deleteById(noteId);
        log.info("笔记删除成功: noteId={}", noteId);
    }

    @Override
    public Map<String, Object> getNoteStats(Long merchantId, Long noteId) {
        log.info("获取笔记统计: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("viewCount", note.getViewCount());
        stats.put("likeCount", note.getLikeCount());
        stats.put("commentCount", note.getCommentCount());
        stats.put("favoriteCount", note.getFavoriteCount());
        stats.put("shareCount", note.getShareCount());
        
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
    
    private List<Long> getShopIdsByMerchant(Long merchantId) {
        // 这里简化处理，实际应该根据商家ID查询关联的门店
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(Shop::getMerchantId, merchantId);
        List<Shop> shops = shopMapper.selectList(wrapper);
        return shops.stream().map(Shop::getId).collect(Collectors.toList());
    }
    
    private PageResult<NoteItemResponse> emptyPageResult(Integer pageNum, Integer pageSize) {
        PageResult<NoteItemResponse> result = new PageResult<>();
        result.setList(new ArrayList<>());
        result.setTotal(0L);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
    
    private NoteItemResponse convertToNoteItemResponse(Note note) {
        NoteItemResponse response = new NoteItemResponse();
        response.setId(note.getId().toString());
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setImage(note.getCoverImage());
        response.setCoverImage(note.getCoverImage());
        response.setLikes(note.getLikeCount());
        response.setViews(note.getViewCount());
        response.setComments(note.getCommentCount());
        response.setTag(note.getTagType());
        response.setStatus(note.getStatus());
        response.setSyncStatus(note.getSyncStatus());
        response.setSyncedToApp(note.getSyncStatus() != null && note.getSyncStatus() == 1);
        response.setNoteType(note.getNoteType());
        response.setCreatedAt(note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
        
        // 获取作者信息
        if (note.getUserId() != null) {
            User user = userMapper.selectById(note.getUserId());
            if (user != null) {
                response.setAuthor(user.getUsername());
                response.setAuthorAvatar(user.getAvatar());
                response.setAuthorId(user.getId().toString());
            }
        }
        
        // 获取门店信息
        if (note.getShopId() != null) {
            Shop shop = shopMapper.selectById(note.getShopId());
            if (shop != null) {
                response.setShopId(shop.getId().toString());
                response.setShopName(shop.getName());
            }
        }
        
        return response;
    }
    
    private NoteDetailResponse convertToNoteDetailResponse(Note note) {
        NoteDetailResponse response = new NoteDetailResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setImage(note.getCoverImage());
        response.setCoverImage(note.getCoverImage());
        response.setLikeCount(note.getLikeCount());
        response.setCommentCount(note.getCommentCount());
        response.setViewCount(note.getViewCount());
        response.setFavoriteCount(note.getFavoriteCount());
        response.setShareCount(note.getShareCount());
        response.setLocation(note.getLocation());
        response.setLatitude(note.getLatitude());
        response.setLongitude(note.getLongitude());
        response.setStatus(note.getStatus());
        response.setSyncStatus(note.getSyncStatus());
        response.setNoteType(note.getNoteType());
        response.setCreatedAt(note.getCreatedAt());
        response.setIsLiked(false);
        response.setIsBookmarked(false);
        response.setIsFollowing(false);
        response.setIsAuthor(true);  // 商家查看自己的笔记
        
        // 处理图片列表
        if (note.getImages() != null && !note.getImages().isEmpty()) {
            response.setImages(Arrays.asList(note.getImages().split(",")));
        }
        
        // 获取作者信息
        if (note.getUserId() != null) {
            User user = userMapper.selectById(note.getUserId());
            if (user != null) {
                response.setAuthorId(user.getId());
                response.setAuthor(user.getUsername());
                response.setAuthorName(user.getUsername());
                response.setAuthorAvatar(user.getAvatar());
            }
        }
        
        // 获取门店信息
        if (note.getShopId() != null) {
            Shop shop = shopMapper.selectById(note.getShopId());
            if (shop != null) {
                response.setShopId(shop.getId());
                response.setShopName(shop.getName());
            }
        }
        
        return response;
    }

    @Override
    public PageResult<Map<String, Object>> getNoteComments(Long merchantId, Long noteId, Integer pageNum, Integer pageSize) {
        log.info("获取笔记评论: merchantId={}, noteId={}, pageNum={}, pageSize={}", merchantId, noteId, pageNum, pageSize);
        
        validateMerchant(merchantId);
        
        // 验证笔记存在
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        // 查询评论列表（只查询顶级评论）
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteComment::getNoteId, noteId)
               .isNull(NoteComment::getParentId)
               .eq(NoteComment::getStatus, 1)
               .orderByDesc(NoteComment::getCreatedAt);
        
        Page<NoteComment> page = new Page<>(pageNum, pageSize);
        Page<NoteComment> commentPage = noteCommentMapper.selectPage(page, wrapper);
        
        // 转换为响应格式
        List<Map<String, Object>> list = commentPage.getRecords().stream()
                .map(comment -> convertCommentToMap(comment, noteId))
                .collect(Collectors.toList());
        
        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setList(list);
        result.setTotal(commentPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    @Transactional
    public Long createNoteComment(Long merchantId, Long operatorId, Long noteId, Map<String, Object> request) {
        log.info("商家发表评论: merchantId={}, noteId={}", merchantId, noteId);
        
        validateMerchant(merchantId);
        
        // 验证笔记存在
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(40404, "笔记不存在");
        }
        
        // 确保商家用户存在
        Long userId = ensureMerchantUserExists(merchantId, operatorId);
        
        // 创建评论
        NoteComment comment = new NoteComment();
        comment.setNoteId(noteId);
        comment.setUserId(userId);
        comment.setContent((String) request.get("content"));
        
        // 处理父评论ID
        if (request.get("parentId") != null) {
            comment.setParentId(Long.valueOf(request.get("parentId").toString()));
        }
        
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        
        noteCommentMapper.insert(comment);
        
        // 更新笔记评论数
        note.setCommentCount((note.getCommentCount() != null ? note.getCommentCount() : 0) + 1);
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        
        log.info("评论创建成功: commentId={}", comment.getId());
        return comment.getId();
    }

    private Map<String, Object> convertCommentToMap(NoteComment comment, Long noteId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("noteId", comment.getNoteId());
        map.put("content", comment.getContent());
        map.put("likeCount", comment.getLikeCount());
        map.put("createdAt", comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null);
        
        // 获取用户信息
        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                map.put("userId", user.getId());
                map.put("userName", user.getUsername());
                map.put("userAvatar", user.getAvatar());
            }
        }
        
        // 获取子评论（回复）
        LambdaQueryWrapper<NoteComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(NoteComment::getNoteId, noteId)
                   .eq(NoteComment::getParentId, comment.getId())
                   .eq(NoteComment::getStatus, 1)
                   .orderByAsc(NoteComment::getCreatedAt)
                   .last("LIMIT 3");  // 只显示前3条回复
        
        List<NoteComment> replies = noteCommentMapper.selectList(replyWrapper);
        List<Map<String, Object>> replyList = replies.stream()
                .map(reply -> {
                    Map<String, Object> replyMap = new HashMap<>();
                    replyMap.put("id", reply.getId());
                    replyMap.put("content", reply.getContent());
                    replyMap.put("createdAt", reply.getCreatedAt() != null ? reply.getCreatedAt().toString() : null);
                    
                    if (reply.getUserId() != null) {
                        User replyUser = userMapper.selectById(reply.getUserId());
                        if (replyUser != null) {
                            replyMap.put("userId", replyUser.getId());
                            replyMap.put("userName", replyUser.getUsername());
                            replyMap.put("userAvatar", replyUser.getAvatar());
                        }
                    }
                    return replyMap;
                })
                .collect(Collectors.toList());
        
        map.put("replies", replyList);
        
        return map;
    }
    
    /**
     * 根据商家ID查找关联的用户ID
     * 通过商家联系电话匹配用户手机号
     */
    private Long findUserIdByMerchantId(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        
        // 查询商家信息
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getContactPhone() == null) {
            return null;
        }
        
        // 通过手机号查找用户
        User user = userMapper.selectByPhone(merchant.getContactPhone());
        if (user != null) {
            log.info("商家关联到用户: merchantId={}, userId={}, phone={}", merchantId, user.getId(), merchant.getContactPhone());
            return user.getId();
        }
        
        return null;
    }
}

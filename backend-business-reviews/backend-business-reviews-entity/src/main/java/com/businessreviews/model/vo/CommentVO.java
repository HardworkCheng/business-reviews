package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论详情展示对象
 * <p>
 * 用于笔记详情页或评论列表页展示，包含用户信息、点赞状态及子评论（回复）
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class CommentVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String author;

    private Long authorId;

    private String avatar;

    private String content;

    private LocalDateTime time;

    private String timeAgo;

    private Integer likes;

    private Boolean liked;

    private Integer replyCount;

    private String replyToUser;

    private List<CommentVO> replies;

    // 商家运营中心专用字段
    private String noteTitle; // 笔记标题（用于显示关联商品）

    private String status; // 状态：published/deleted

    private String reply; // 商家回复内容

    private LocalDateTime replyTime; // 商家回复时间

    private Boolean isTop; // 是否置顶

    private Boolean isVip; // 是否VIP用户

    private Boolean isAnonymous; // 是否匿名

    private Double rating; // 综合评分

    private Double tasteScore; // 口味评分

    private Double environmentScore; // 环境评分

    private Double serviceScore; // 服务评分

    private List<String> images; // 评论图片
}

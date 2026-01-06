package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读消息计数展示对象
 * <p>
 * 包含各类型消息（点赞、评论、关注、系统、私信）的未读统计
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class UnreadCountVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer total;
    private Integer messageCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer followCount;
    private Integer systemCount;
}

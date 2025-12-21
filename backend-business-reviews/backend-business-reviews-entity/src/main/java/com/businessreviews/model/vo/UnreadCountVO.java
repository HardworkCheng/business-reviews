package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读消息数VO
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

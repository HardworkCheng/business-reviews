package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 浏览历史项VO
 */
@Data
public class HistoryItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer type;  // 1=笔记, 2=商家
    private String targetId;
    private String image;
    private String title;
    private String viewTime;  // 浏览时间
    private String createdAt;
}

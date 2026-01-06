package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 浏览历史列表项对象
 * <p>
 * 记录用户浏览过的笔记或商家，包含浏览时间字段
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class HistoryItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer type; // 1=笔记, 2=商家
    private String targetId;
    private String image;
    private String title;
    private String author; // 作者名称
    private Long authorId; // 作者ID
    private String viewTime; // 浏览时间
    private String createdAt;
}

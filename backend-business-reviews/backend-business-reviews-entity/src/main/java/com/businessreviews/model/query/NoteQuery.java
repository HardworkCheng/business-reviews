package com.businessreviews.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 笔记查询参数对象
 * <p>
 * 封装笔记列表的筛选条件，支持关键词、分类、状态及排序等多维度查询
 * </p>
 * 
 * @author businessreviews
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoteQuery extends BaseQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词（标题/内容）
     */
    private String keyword;

    /**
     * 作者用户ID
     */
    private Long userId;

    /**
     * 关联商家ID
     */
    private Long shopId;

    /**
     * 笔记类型（1用户笔记，2商家笔记）
     */
    private Integer noteType;

    /**
     * 状态筛选（1正常，2隐藏，3审核中）
     */
    private Integer status;

    /**
     * 标签类型（hot/discount/new）
     */
    private String tagType;

    /**
     * 是否推荐
     */
    private Boolean recommend;

    /**
     * 排序字段（createdAt/likeCount/viewCount）
     */
    private String sortBy;

    /**
     * 排序方向（asc/desc）
     */
    private String sortOrder;
}

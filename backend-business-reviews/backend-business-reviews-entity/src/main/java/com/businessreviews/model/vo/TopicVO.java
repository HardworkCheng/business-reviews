package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 话题展示对象
 * <p>
 * 用于展示话题信息及其热度指标（浏览数、笔记数）
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class TopicVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String description;
    private String coverImage;
    private Integer noteCount;
    private Integer viewCount;

    /**
     * 是否热门
     */
    @JsonProperty("isHot")
    private Boolean hot;
}

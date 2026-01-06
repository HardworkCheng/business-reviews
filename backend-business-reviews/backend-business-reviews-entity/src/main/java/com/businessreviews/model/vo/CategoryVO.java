package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 笔记分类展示对象
 * <p>
 * 用于前端首页分类导航栏展示（如：美食、景点、住宿等）
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class CategoryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
}

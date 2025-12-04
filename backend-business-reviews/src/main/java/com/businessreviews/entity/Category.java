package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家分类实体类
 */
@Data
@TableName("categories")
public class Category implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 主题颜色
     */
    private String color;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 状态（1启用，2禁用）
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

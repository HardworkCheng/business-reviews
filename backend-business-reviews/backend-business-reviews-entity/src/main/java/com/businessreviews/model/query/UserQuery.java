package com.businessreviews.model.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户查询对象
 * 封装用户查询参数
 * 
 * @author businessreviews
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends BaseQuery {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 搜索关键词（用户名/手机号）
     */
    private String keyword;
    
    /**
     * 状态筛选（1正常，2禁用）
     */
    private Integer status;
    
    /**
     * 性别筛选（0未知，1男，2女）
     */
    private Integer gender;
    
    /**
     * 注册开始日期
     */
    private LocalDate startDate;
    
    /**
     * 注册结束日期
     */
    private LocalDate endDate;
}

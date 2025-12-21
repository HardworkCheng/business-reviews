package com.businessreviews.model.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础查询对象
 * 包含通用的分页参数
 * 
 * @author businessreviews
 */
@Data
public class BaseQuery implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;
}

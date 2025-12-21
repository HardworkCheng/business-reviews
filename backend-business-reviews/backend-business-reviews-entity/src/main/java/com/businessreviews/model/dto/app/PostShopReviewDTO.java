package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端发表商家评价请求DTO
 */
@Data
public class PostShopReviewDTO {
    
    @NotNull(message = "综合评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低1分")
    @DecimalMax(value = "5.0", message = "评分最高5分")
    private BigDecimal rating;
    
    @NotNull(message = "口味评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低1分")
    @DecimalMax(value = "5.0", message = "评分最高5分")
    private BigDecimal tasteScore;
    
    @NotNull(message = "环境评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低1分")
    @DecimalMax(value = "5.0", message = "评分最高5分")
    private BigDecimal environmentScore;
    
    @NotNull(message = "服务评分不能为空")
    @DecimalMin(value = "1.0", message = "评分最低1分")
    @DecimalMax(value = "5.0", message = "评分最高5分")
    private BigDecimal serviceScore;
    
    @NotBlank(message = "评价内容不能为空")
    @Size(min = 10, max = 500, message = "评价内容长度应在10-500之间")
    private String content;
    
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}

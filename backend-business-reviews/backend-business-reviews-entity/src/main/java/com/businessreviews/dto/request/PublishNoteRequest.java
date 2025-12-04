package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布笔记请求
 */
@Data
public class PublishNoteRequest {
    
    @NotBlank(message = "笔记标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度应在2-100之间")
    private String title;
    
    @NotBlank(message = "笔记内容不能为空")
    @Size(min = 10, max = 5000, message = "内容长度应在10-5000之间")
    private String content;
    
    @NotEmpty(message = "至少上传一张图片")
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
    
    private Long shopId;
    
    private String location;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private List<String> tags;
    
    private List<Long> topics;
}

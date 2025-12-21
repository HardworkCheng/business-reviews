package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端发布笔记请求DTO
 */
@Data
public class PublishNoteDTO {
    
    @Size(max = 100, message = "标题长度不能超过100字")
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
    
    private Integer status; // 1=公开，2=仅自己可见
}

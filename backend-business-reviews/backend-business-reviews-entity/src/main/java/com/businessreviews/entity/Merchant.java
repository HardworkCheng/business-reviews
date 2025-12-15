package com.businessreviews.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商家实体类
 */
@Data
@TableName("merchants")
public class Merchant {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 商家名称/企业名称 */
    private String name;
    
    /** 商家Logo */
    private String logo;
    
    /** 营业执照号 */
    private String licenseNo;
    
    /** 营业执照图片 */
    private String licenseImage;
    
    /** 联系人姓名 */
    private String contactName;
    
    /** 联系电话 */
    private String contactPhone;
    
    /** 联系邮箱 */
    private String contactEmail;
    
    /** 状态（1正常，2禁用，3待审核） */
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

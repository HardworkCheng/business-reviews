package com.businessreviews.model.dto.merchant;

import lombok.Data;

/**
 * 商家信息更新请求DTO
 */
@Data
public class MerchantUpdateDTO {

    /** 商家名称 */
    private String merchantName;

    /** 商家负责人姓名 */
    private String merchantOwnerName;

    /** 商家头像 */
    private String avatar;

    /** 联系邮箱 */
    private String contactEmail;

    /** 联系电话 */
    private String phone;

    /** 营业执照号 */
    private String licenseNo;

    /** 营业执照图片 */
    private String licenseImage;
}

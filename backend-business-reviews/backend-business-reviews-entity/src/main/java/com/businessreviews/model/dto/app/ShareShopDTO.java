package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分享店铺请求DTO
 */
@Data
public class ShareShopDTO {

    /**
     * 店铺ID
     */
    @NotNull(message = "店铺ID不能为空")
    private Long shopId;

    /**
     * 接收者用户ID列表
     */
    @NotEmpty(message = "接收者列表不能为空")
    private List<Long> userIds;
}

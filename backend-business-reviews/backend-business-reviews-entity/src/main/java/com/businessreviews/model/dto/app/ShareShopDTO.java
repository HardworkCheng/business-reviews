package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 商家分享请求传输对象
 * <p>
 * 将商家主页信息分享给站内好友。
 * </p>
 * 
 * @author businessreviews
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

package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关注用户请求传输对象
 * <p>
 * 专门用于关注其他用户的操作
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class FollowUserDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}

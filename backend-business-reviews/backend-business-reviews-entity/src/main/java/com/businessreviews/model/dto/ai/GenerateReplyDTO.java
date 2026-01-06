package com.businessreviews.model.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI智能回复生成请求传输对象
 * <p>
 * 封装用户评价内容及可选的补偿策略，用于请求AI生成建议回复
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class GenerateReplyDTO {

    /**
     * 用户的差评内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字")
    private String reviewText;

    /**
     * 商家的补偿策略（可选）
     * 例如：送一张8折券、送两杯饮料、下次到店送小菜
     */
    @Size(max = 200, message = "补偿策略不能超过200字")
    private String strategy;
}

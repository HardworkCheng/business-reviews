package com.businessreviews.service.ai;

/**
 * 差评智能回复服务接口
 * 
 * @author businessreviews
 */
public interface ReviewReplyService {

    /**
     * 生成差评回复
     * 
     * @param reviewText 用户的差评内容
     * @param strategy   商家的补偿策略（可选）
     * @return 生成的回复文案
     */
    String generateReply(String reviewText, String strategy);
}

package com.businessreviews.model.dto.ai;

import com.businessreviews.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI内容审核结果对象
 * 用于LangChain4j的结构化输出映射
 * 
 * 设计说明：
 * 1. LangChain4j会自动将此类的结构（Schema）传递给大模型
 * 2. 模型会按照此结构输出JSON数据
 * 3. 框架自动完成反序列化，代码无需手动解析JSON
 * 
 * @author businessreviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResult {

    /**
     * 是否安全
     * true: 内容安全，可以发布
     * false: 内容违规，需要拦截
     */
    private boolean isSafe;

    /**
     * 违规类型
     * 
     * @see ViolationType
     */
    private ViolationType type;

    /**
     * 违规的具体原因说明（20字以内）
     * 例如："检测到微信号引流信息"
     */
    private String reason;

    /**
     * 给用户的整改建议
     * 例如："请移除微信号后重试"
     * 当内容安全时，此字段为空或提示"内容合规"
     */
    private String suggestion;

    /**
     * 创建安全结果的快捷方法
     */
    public static AuditResult safe() {
        return AuditResult.builder()
                .isSafe(true)
                .type(ViolationType.SAFE)
                .reason("内容安全")
                .suggestion("内容合规，可以发布")
                .build();
    }

    /**
     * 创建违规结果的快捷方法
     */
    public static AuditResult violation(ViolationType type, String reason, String suggestion) {
        return AuditResult.builder()
                .isSafe(false)
                .type(type)
                .reason(reason)
                .suggestion(suggestion)
                .build();
    }
}

package com.businessreviews.enums;

import lombok.Getter;

/**
 * 违规类型枚举
 * 用于AI内容审核系统识别和分类违规内容
 * 
 * @author businessreviews
 */
@Getter
public enum ViolationType {

    /**
     * 安全内容 - 内容正常，无违规
     */
    SAFE("安全内容", "内容符合社区规范"),

    /**
     * 广告引流 - 包含微信号、二维码、兼职刷单等
     * 注意：校园卡转让、失物招领属于正常内容，不是广告
     */
    ADVERTISEMENT("广告引流", "内容包含广告推广、微信号、二维码等引流信息"),

    /**
     * 攻击谩骂 - 针对个人的辱骂、人身攻击、网络暴力
     */
    ABUSE("攻击谩骂", "内容包含人身攻击、辱骂、网络暴力等"),

    /**
     * 敏感内容 - 色情低俗或敏感政治话题
     */
    SENSITIVE("敏感内容", "内容包含色情低俗或政治敏感信息");

    private final String name;
    private final String description;

    ViolationType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

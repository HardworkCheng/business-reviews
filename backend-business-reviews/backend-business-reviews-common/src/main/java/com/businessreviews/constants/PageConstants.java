package com.businessreviews.constants;

/**
 * 分页参数默认配置常量
 * <p>
 * 定义系统默认的分页策略，防止恶意的大页请求
 * </p>
 */
public final class PageConstants {

    private PageConstants() {
        // 私有构造函数，防止实例化
    }

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页数量 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大每页数量 */
    public static final int MAX_PAGE_SIZE = 50;
}

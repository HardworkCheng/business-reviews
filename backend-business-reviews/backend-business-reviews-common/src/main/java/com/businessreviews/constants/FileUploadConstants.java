package com.businessreviews.constants;

/**
 * 文件上传常量
 * 
 * @author Kiro
 * @since 2025-12-25
 */
public class FileUploadConstants {
    
    private FileUploadConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * 允许的图片文件扩展名
     */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    };
    
    /**
     * 最大文件大小: 10MB
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 最大文件大小描述
     */
    public static final String MAX_FILE_SIZE_DESC = "10MB";
    
    /**
     * 文件大小单位
     */
    public static final int SIZE_1KB = 1024;
    public static final int SIZE_1MB = 1024 * 1024;
    public static final long SIZE_1GB = 1024 * 1024 * 1024L;
}

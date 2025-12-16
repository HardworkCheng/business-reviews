package com.businessreviews.service.common;

import org.springframework.web.multipart.MultipartFile;

/**
 * 通用上传服务接口
 * 提供图片上传功能，供Mobile端和Merchant端共用
 */
public interface UploadService {
    
    /**
     * 上传单个图片
     */
    String uploadImage(MultipartFile file, Long userId);
    
    /**
     * 上传多个图片
     */
    String[] uploadImages(MultipartFile[] files, Long userId);
    
    /**
     * 上传头像
     */
    String uploadAvatar(MultipartFile file, Long userId);
    
    /**
     * 删除图片
     */
    void deleteImage(String imageUrl);
}

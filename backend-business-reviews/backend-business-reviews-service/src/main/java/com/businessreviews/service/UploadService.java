package com.businessreviews.service;

import org.springframework.web.multipart.MultipartFile;

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

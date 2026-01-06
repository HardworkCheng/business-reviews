package com.businessreviews.service.common;

import org.springframework.web.multipart.MultipartFile;

/**
 * 通用上传服务接口
 * <p>
 * 提供图片上传功能，供Mobile端和Merchant端共用
 * </p>
 * 
 * @author businessreviews
 */
public interface UploadService {

    /**
     * 上传单个图片
     * 
     * @param file   图片文件
     * @param userId 上传用户ID
     * @return 图片访问URL
     */
    String uploadImage(MultipartFile file, Long userId);

    /**
     * 上传多个图片
     * 
     * @param files  图片文件数组
     * @param userId 上传用户ID
     * @return 图片访问URL数组
     */
    String[] uploadImages(MultipartFile[] files, Long userId);

    /**
     * 上传头像
     * 
     * @param file   头像文件
     * @param userId 用户ID
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 删除图片
     * 
     * @param imageUrl 图片URL
     */
    void deleteImage(String imageUrl);
}

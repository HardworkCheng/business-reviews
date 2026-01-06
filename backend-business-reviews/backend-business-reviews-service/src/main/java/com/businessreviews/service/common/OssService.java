package com.businessreviews.service.common;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * OSS文件上传服务接口
 * <p>
 * 提供阿里云OSS文件上传功能，供Mobile端和Merchant端共用
 * </p>
 * 
 * @author businessreviews
 */
public interface OssService {

    /**
     * 上传文件到OSS
     * 
     * @param file   文件
     * @param folder 文件夹路径(如: avatars, images)
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 上传文件流到OSS
     * 
     * @param inputStream 文件流
     * @param fileName    文件名
     * @param folder      文件夹路径
     * @return 文件访问URL
     */
    String uploadStream(InputStream inputStream, String fileName, String folder);

    /**
     * 删除OSS文件
     * 
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}

package com.businessreviews.manager;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.businessreviews.config.OssConfig;
import com.businessreviews.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * OSS文件管理器
 * 负责阿里云OSS的文件上传、删除等操作
 * 
 * @author businessreviews
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssManager {
    
    private final OssConfig ossConfig;
    
    /**
     * 允许的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};
    
    /**
     * 最大文件大小: 10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 上传文件到OSS
     *
     * @param file   文件
     * @param folder 文件夹路径
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String objectKey = buildObjectKey(folder, fileName);
            return uploadToOss(file.getInputStream(), objectKey);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(50000, "文件上传失败");
        }
    }
    
    /**
     * 上传输入流到OSS
     *
     * @param inputStream 输入流
     * @param fileName    文件名
     * @param folder      文件夹路径
     * @return 文件URL
     */
    public String uploadStream(InputStream inputStream, String fileName, String folder) {
        String objectKey = buildObjectKey(folder, fileName);
        return uploadToOss(inputStream, objectKey);
    }
    
    /**
     * 删除OSS文件
     *
     * @param fileUrl 文件URL
     */
    public void removeFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        OSS ossClient = null;
        try {
            String objectKey = extractObjectKey(fileUrl);
            if (objectKey == null) {
                return;
            }
            
            ossClient = createOssClient();
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("OSS文件删除成功: {}", objectKey);
            
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", fileUrl, e);
        } finally {
            shutdownOssClient(ossClient);
        }
    }
    
    /**
     * 上传到阿里云OSS
     */
    private String uploadToOss(InputStream inputStream, String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = createOssClient();
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(),
                objectKey,
                inputStream
            );
            ossClient.putObject(putObjectRequest);
            
            String fileUrl = ossConfig.getUrlPrefix() + "/" + objectKey;
            log.info("文件上传成功到OSS: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("OSS上传失败: {}", objectKey, e);
            throw new BusinessException(50000, "OSS上传失败: " + e.getMessage());
        } finally {
            shutdownOssClient(ossClient);
        }
    }
    
    /**
     * 创建OSS客户端
     */
    private OSS createOssClient() {
        return new OSSClientBuilder().build(
            ossConfig.getEndpoint(),
            ossConfig.getAccessKeyId(),
            ossConfig.getAccessKeySecret()
        );
    }
    
    /**
     * 关闭OSS客户端
     */
    private void shutdownOssClient(OSS ossClient) {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40001, "文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(40001, "文件大小不能超过10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(40001, "文件名不能为空");
        }
        
        String extension = getExtension(originalFilename).toLowerCase();
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new BusinessException(40001, "仅支持JPG、PNG、GIF、WEBP、BMP格式的图片");
        }
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
    
    /**
     * 构建对象key (带日期目录)
     */
    private String buildObjectKey(String folder, String fileName) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return folder + "/" + date + "/" + fileName;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        return lastDot == -1 ? "" : filename.substring(lastDot);
    }
    
    /**
     * 从URL中提取ObjectKey
     */
    private String extractObjectKey(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(ossConfig.getUrlPrefix())) {
            return null;
        }
        return fileUrl.substring(ossConfig.getUrlPrefix().length() + 1);
    }
}

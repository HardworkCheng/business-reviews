package com.businessreviews.service.impl.common;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.businessreviews.config.OssConfig;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.service.common.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {
    
    private final OssConfig ossConfig;
    
    /**
     * 允许的文件类型
     */
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};
    
    /**
     * 最大文件大小: 10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        // 验证文件
        validateFile(file);
        
        try {
            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String objectKey = buildObjectKey(folder, fileName);
            
            // 将文件内容读取到字节数组，避免 InputStream 重置问题
            byte[] fileBytes = file.getBytes();
            
            // 使用字节数组创建新的 InputStream 上传到 OSS
            return uploadToOss(new ByteArrayInputStream(fileBytes), objectKey, fileBytes.length);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(50000, "文件上传失败");
        }
    }
    
    @Override
    public String uploadStream(InputStream inputStream, String fileName, String folder) {
        try {
            String objectKey = buildObjectKey(folder, fileName);
            
            // 将输入流读取到字节数组，避免重置问题
            byte[] bytes = inputStream.readAllBytes();
            
            // 使用字节数组创建新的 InputStream 上传到 OSS
            return uploadToOss(new ByteArrayInputStream(bytes), objectKey, bytes.length);
        } catch (IOException e) {
            log.error("流上传失败", e);
            throw new BusinessException(50000, "流上传失败");
        }
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        // 直接删除OSS文件
        deleteOssFile(fileUrl);
    }
    
    /**
     * 上传到阿里云OSS
     */
    private String uploadToOss(InputStream inputStream, String objectKey, long contentLength) {
        OSS ossClient = null;
        try {
            // 创建OSSClient实例
            ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
            );
            
            // 创建上传请求，设置内容长度
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossConfig.getBucketName(),
                objectKey,
                inputStream
            );
            
            // 设置元数据，指定内容长度
            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentLength(contentLength);
            putObjectRequest.setMetadata(metadata);
            
            // 上传文件
            ossClient.putObject(putObjectRequest);
            
            // 返回文件URL
            String fileUrl = ossConfig.getUrlPrefix() + "/" + objectKey;
            log.info("文件上传成功到OSS: {}", fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("OSS上传失败: {}", objectKey, e);
            throw new BusinessException(50000, "OSS上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            // 关闭输入流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
    }
    
    /**
     * 删除OSS文件
     */
    private void deleteOssFile(String fileUrl) {
        OSS ossClient = null;
        try {
            String objectKey = extractObjectKey(fileUrl);
            if (objectKey == null) {
                return;
            }
            
            ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
            );
            
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("OSS文件删除成功: {}", objectKey);
            
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", fileUrl, e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40001, "文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(40001, "文件大小不能超过10MB");
        }
        
        // 验证文件类型
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

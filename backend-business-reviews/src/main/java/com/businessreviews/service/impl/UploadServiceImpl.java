package com.businessreviews.service.impl;

import com.businessreviews.exception.BusinessException;
import com.businessreviews.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080/static}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    @Override
    public String uploadImage(MultipartFile file, Long userId) {
        validateFile(file);
        
        String fileName = generateFileName(file.getOriginalFilename());
        String relativePath = "images/" + userId + "/" + fileName;
        
        try {
            Path targetPath = Paths.get(uploadPath, relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            
            return baseUrl + "/" + relativePath;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(50000, "文件上传失败");
        }
    }

    @Override
    public String[] uploadImages(MultipartFile[] files, Long userId) {
        if (files == null || files.length == 0) {
            throw new BusinessException(40001, "请选择要上传的图片");
        }
        
        if (files.length > 9) {
            throw new BusinessException(40001, "最多上传9张图片");
        }
        
        String[] urls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = uploadImage(files[i], userId);
        }
        return urls;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        validateFile(file);
        
        String fileName = "avatar_" + System.currentTimeMillis() + getExtension(file.getOriginalFilename());
        String relativePath = "avatars/" + userId + "/" + fileName;
        
        try {
            Path targetPath = Paths.get(uploadPath, relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            
            return baseUrl + "/" + relativePath;
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BusinessException(50000, "头像上传失败");
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
            return;
        }
        
        String relativePath = imageUrl.substring(baseUrl.length() + 1);
        Path filePath = Paths.get(uploadPath, relativePath);
        
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", imageUrl, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40001, "请选择要上传的文件");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(40301, "文件大小超过限制（最大10MB）");
        }
        
        String extension = getExtension(file.getOriginalFilename());
        if (extension == null || Arrays.stream(ALLOWED_EXTENSIONS)
                .noneMatch(ext -> ext.equalsIgnoreCase(extension.substring(1)))) {
            throw new BusinessException(40302, "不支持的文件格式");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = getExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return ".jpg";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return ".jpg";
        }
        return filename.substring(dotIndex);
    }
}

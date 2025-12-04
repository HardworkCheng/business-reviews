package com.businessreviews.service.impl;

import com.businessreviews.exception.BusinessException;
import com.businessreviews.service.OssService;
import com.businessreviews.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final OssService ossService;

    @Override
    public String uploadImage(MultipartFile file, Long userId) {
        log.info("用户{}上传图片: {}", userId, file.getOriginalFilename());
        return ossService.uploadFile(file, "images");
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
        log.info("用户{}上传头像: {}", userId, file.getOriginalFilename());
        return ossService.uploadFile(file, "avatars");
    }

    @Override
    public void deleteImage(String imageUrl) {
        log.info("删除图片: {}", imageUrl);
        ossService.deleteFile(imageUrl);
    }
}

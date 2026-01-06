package com.businessreviews.service.impl.common;

import com.businessreviews.exception.BusinessException;
import com.businessreviews.service.common.OssService;
import com.businessreviews.service.common.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传服务实现类
 * <p>
 * 提供图片上传（单图、多图、头像）及删除服务的统一封装。
 * 底层调用 {@link OssService} 进行实际的文件存储。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final OssService ossService;

    /**
     * 上传单张图片
     * <p>
     * 将图片上传到"images"目录。
     * </p>
     *
     * @param file   图片文件
     * @param userId 当前用户ID（仅用于日志记录）
     * @return 图片完整URL
     */
    @Override
    public String uploadImage(MultipartFile file, Long userId) {
        log.info("用户{}上传图片: {}", userId, file.getOriginalFilename());
        return ossService.uploadFile(file, "images");
    }

    /**
     * 批量上传图片
     * <p>
     * 循环调用单张上传接口。
     * 限制一次最多上传9张图片。
     * </p>
     *
     * @param files  图片文件数组
     * @param userId 当前用户ID
     * @return 图片URL数组
     * @throws BusinessException 如果未选择文件或超过数量限制(40001)
     */
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

    /**
     * 上传用户头像
     * <p>
     * 将图片上传到"avatars"目录。
     * </p>
     *
     * @param file   图片文件
     * @param userId 当前用户ID
     * @return 头像完整URL
     */
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        log.info("用户{}上传头像: {}", userId, file.getOriginalFilename());
        return ossService.uploadFile(file, "avatars");
    }

    /**
     * 删除图片
     *
     * @param imageUrl 图片完整URL
     */
    @Override
    public void deleteImage(String imageUrl) {
        log.info("删除图片: {}", imageUrl);
        ossService.deleteFile(imageUrl);
    }
}

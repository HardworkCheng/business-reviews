package com.businessreviews.merchant.controller;

import com.businessreviews.common.Result;
import com.businessreviews.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchant/upload")
@RequiredArgsConstructor
public class MerchantUploadController {

    private final OssService ossService;

    /**
     * 上传单个文件
     */
    @PostMapping
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "merchant") String folder) {
        try {
            String url = ossService.uploadFile(file, folder);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/batch")
    public Result<Map<String, List<String>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", defaultValue = "merchant") String folder) {
        try {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                String url = ossService.uploadFile(file, folder);
                urls.add(url);
            }
            Map<String, List<String>> result = new HashMap<>();
            result.put("urls", urls);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量文件上传失败", e);
            return Result.error("批量文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 公开上传接口（用于商家入驻注册时上传图片，无需登录）
     * 仅允许上传到 merchant/logo, merchant/avatar, merchant/license 目录
     */
    @PostMapping("/public")
    public Result<Map<String, String>> uploadPublicFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "merchant/register") String folder) {
        try {
            // 安全检查：只允许上传到注册相关的目录
            if (!folder.startsWith("merchant/logo") && 
                !folder.startsWith("merchant/avatar") && 
                !folder.startsWith("merchant/license") &&
                !folder.startsWith("merchant/register")) {
                return Result.error("不允许上传到该目录");
            }
            
            String url = ossService.uploadFile(file, folder);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            log.info("公开上传成功: folder={}, url={}", folder, url);
            return Result.success(result);
        } catch (Exception e) {
            log.error("公开文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}

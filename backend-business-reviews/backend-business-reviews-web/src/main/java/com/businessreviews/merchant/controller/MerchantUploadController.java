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
}

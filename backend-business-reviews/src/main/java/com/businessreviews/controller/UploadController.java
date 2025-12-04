package com.businessreviews.controller;

import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.requireUserId();
        String url = uploadService.uploadImage(file, userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }

    /**
     * 上传多个图片
     */
    @PostMapping("/images")
    public Result<Map<String, String[]>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        Long userId = UserContext.requireUserId();
        String[] urls = uploadService.uploadImages(files, userId);
        
        Map<String, String[]> data = new HashMap<>();
        data.put("urls", urls);
        return Result.success("上传成功", data);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.requireUserId();
        String url = uploadService.uploadAvatar(file, userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }
}

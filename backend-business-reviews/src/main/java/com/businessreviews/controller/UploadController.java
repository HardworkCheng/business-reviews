package com.businessreviews.controller;

import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.service.UploadService;
import com.businessreviews.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtUtil jwtUtil;

    /**
     * 上传单个图片
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        String url = uploadService.uploadImage(file, userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }

    /**
     * 上传多个图片
     */
    @PostMapping("/images")
    public Result<Map<String, String[]>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        String[] urls = uploadService.uploadImages(files, userId);
        
        Map<String, String[]> data = new HashMap<>();
        data.put("urls", urls);
        return Result.success("上传成功", data);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        String url = uploadService.uploadAvatar(file, userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success("上传成功", data);
    }
    
    /**
     * 手动解析token获取userId
     */
    private Long getUserId(HttpServletRequest httpRequest) {
        // 从 UserContext 获取
        Long userId = UserContext.getUserId();
        
        // 如果 UserContext 中没有,直接从请求头获取
        if (userId == null) {
            String authorization = httpRequest.getHeader("Authorization");
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                
                try {
                    userId = jwtUtil.getUserIdFromToken(token);
                    UserContext.setUserId(userId);
                } catch (Exception e) {
                    throw new RuntimeException("登录已过期,请重新登录");
                }
            } else {
                throw new RuntimeException("未登录");
            }
        }
        
        return userId;
    }
}

package com.businessreviews.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    
    /**
     * OSS服务endpoint
     */
    private String endpoint;
    
    /**
     * 阿里云AccessKeyId
     */
    private String accessKeyId;
    
    /**
     * 阿里云AccessKeySecret
     */
    private String accessKeySecret;
    
    /**
     * Bucket名称
     */
    private String bucketName;
    
    /**
     * URL前缀
     */
    private String urlPrefix;
}

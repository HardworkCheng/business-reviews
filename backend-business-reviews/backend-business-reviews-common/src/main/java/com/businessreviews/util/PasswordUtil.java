package com.businessreviews.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密安全工具类
 * <p>
 * 采用 PBKDF2 思想的简化版实现：
 * 1. 生成16位随机盐值 (Salt)
 * 2. 使用 SHA-256 算法对 (密码 + 盐) 进行哈希
 * 3. 最终存储格式：Base64(盐 + 哈希值)
 * </p>
 */
public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * 加密密码
     * 
     * @param password 原始密码
     * @return 加密后的密码（包含盐值）
     */
    public static String encryptPassword(String password) {
        try {
            // 生成随机盐值
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // 使用SHA-256加密
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 将盐值和加密后的密码组合
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            // Base64编码返回
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     * 
     * @param password          原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String encryptedPassword) {
        try {
            // Base64解码
            byte[] combined = Base64.getDecoder().decode(encryptedPassword);

            // 提取盐值
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);

            // 提取加密后的密码
            byte[] hashedPassword = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, hashedPassword, 0, hashedPassword.length);

            // 使用相同的盐值加密输入的密码
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] testHash = md.digest(password.getBytes());

            // 比较两个加密后的密码
            return MessageDigest.isEqual(testHash, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

package com.businessreviews.service.merchant;

import com.businessreviews.model.dto.merchant.MerchantLoginDTO;
import com.businessreviews.model.dto.merchant.MerchantRegisterDTO;
import com.businessreviews.model.vo.merchant.MerchantLoginVO;
import com.businessreviews.model.vo.merchant.MerchantUserInfoVO;

/**
 * 商家认证服务接口
 * 提供商家运营中心的登录、注册、验证码等功能
 * 已整合merchant_users表到merchants表
 */
public interface MerchantAuthService {
    
    /**
     * 发送验证码
     */
    void sendCode(String phone);
    
    /**
     * 验证码登录
     */
    MerchantLoginVO loginByCode(String phone, String code);
    
    /**
     * 密码登录
     */
    MerchantLoginVO loginByPassword(MerchantLoginDTO request);
    
    /**
     * 获取当前登录商家信息（使用商家ID）
     */
    MerchantUserInfoVO getCurrentUserInfo(Long merchantId);
    
    /**
     * 退出登录
     */
    void logout(String token);
    
    /**
     * 商家入驻注册（完整信息）
     */
    MerchantLoginVO register(MerchantRegisterDTO request);
}

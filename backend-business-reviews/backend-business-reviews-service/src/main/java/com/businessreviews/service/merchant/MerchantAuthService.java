package com.businessreviews.service.merchant;

import com.businessreviews.model.dto.merchant.MerchantLoginDTO;
import com.businessreviews.model.dto.merchant.MerchantRegisterDTO;
import com.businessreviews.model.vo.merchant.MerchantLoginVO;
import com.businessreviews.model.vo.merchant.MerchantUserInfoVO;

/**
 * 商家认证服务接口
 * <p>
 * 提供商家运营中心的登录、注册、验证码等功能
 * 已整合merchant_users表到merchants表
 * </p>
 * 
 * @author businessreviews
 */
public interface MerchantAuthService {

    /**
     * 发送验证码
     * 
     * @param phone 手机号
     */
    void sendCode(String phone);

    /**
     * 验证码登录
     * 
     * @param phone 手机号
     * @param code  验证码
     * @return 登录成功VO（含Token）
     */
    MerchantLoginVO loginByCode(String phone, String code);

    /**
     * 密码登录
     * 
     * @param request 密码登录请求对象
     * @return 登录成功VO（含Token）
     */
    MerchantLoginVO loginByPassword(MerchantLoginDTO request);

    /**
     * 获取当前登录商家信息（使用商家ID）
     * 
     * @param merchantId 商家ID
     * @return 商家详细信息VO
     */
    MerchantUserInfoVO getCurrentUserInfo(Long merchantId);

    /**
     * 退出登录
     * 
     * @param token 登录Token
     */
    void logout(String token);

    /**
     * 商家入驻注册（完整信息）
     * 
     * @param request 注册请求对象
     * @return 注册并登录成功VO
     */
    MerchantLoginVO register(MerchantRegisterDTO request);

    /**
     * 更新商家信息
     * 
     * @param merchantId 商家ID
     * @param request    更新请求对象
     */
    void updateProfile(Long merchantId, com.businessreviews.model.dto.merchant.MerchantUpdateDTO request);
}

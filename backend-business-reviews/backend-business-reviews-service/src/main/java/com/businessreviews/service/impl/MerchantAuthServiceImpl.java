package com.businessreviews.service.impl;

import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.dto.request.MerchantLoginRequest;
import com.businessreviews.dto.response.MerchantLoginResponse;
import com.businessreviews.dto.response.MerchantUserInfoResponse;
import com.businessreviews.entity.Merchant;
import com.businessreviews.entity.MerchantUser;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.MerchantUserMapper;
import com.businessreviews.service.MerchantAuthService;
import com.businessreviews.util.JwtUtil;
import com.businessreviews.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 商家认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantAuthServiceImpl implements MerchantAuthService {

    private final MerchantMapper merchantMapper;
    private final MerchantUserMapper merchantUserMapper;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    private static final String MERCHANT_SMS_CODE_PREFIX = "merchant:sms:code:";
    private static final String MERCHANT_SMS_LIMIT_PREFIX = "merchant:sms:limit:";

    @Override
    public void sendCode(String phone) {
        // 检查发送频率
        String limitKey = MERCHANT_SMS_LIMIT_PREFIX + phone;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40005, "操作过于频繁，请稍后再试");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存储到Redis，5分钟过期
        redisUtil.set(MERCHANT_SMS_CODE_PREFIX + phone, code, 300);
        redisUtil.set(limitKey, "1", 60);

        // TODO: 调用短信服务发送验证码
        log.info("商家端验证码已发送: phone={}, code={}", phone, code);
    }

    @Override
    public MerchantLoginResponse loginByCode(String phone, String code) {
        // 验证验证码
        String cacheCode = redisUtil.get(MERCHANT_SMS_CODE_PREFIX + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            throw new BusinessException(40002, "验证码错误或已过期");
        }

        // 查找商家用户
        MerchantUser user = merchantUserMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException(40401, "账号不存在，请先入驻");
        }

        // 删除验证码
        redisUtil.delete(MERCHANT_SMS_CODE_PREFIX + phone);

        // 更新登录时间
        user.setLastLoginAt(LocalDateTime.now());
        merchantUserMapper.updateById(user);

        // 生成Token并返回
        return buildLoginResponse(user);
    }

    @Override
    public MerchantLoginResponse loginByPassword(MerchantLoginRequest request) {
        MerchantUser user = merchantUserMapper.selectByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException(40401, "账号不存在");
        }

        // 验证密码（实际项目应使用BCrypt加密比对）
        if (!request.getPassword().equals(user.getPassword())) {
            throw new BusinessException(40003, "密码错误");
        }

        // 更新登录时间
        user.setLastLoginAt(LocalDateTime.now());
        merchantUserMapper.updateById(user);

        return buildLoginResponse(user);
    }

    @Override
    public MerchantUserInfoResponse getCurrentUserInfo(Long userId) {
        MerchantUser user = merchantUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        Merchant merchant = merchantMapper.selectById(user.getMerchantId());

        MerchantUserInfoResponse response = new MerchantUserInfoResponse();
        response.setUserId(user.getId().toString());
        response.setMerchantId(user.getMerchantId().toString());
        response.setMerchantName(merchant != null ? merchant.getName() : "");
        response.setMerchantLogo(merchant != null ? merchant.getLogo() : "");
        response.setName(user.getName());
        response.setPhone(maskPhone(user.getPhone()));
        response.setAvatar(user.getAvatar());
        response.setRoleName("管理员"); // TODO: 根据角色ID查询
        response.setPermissions(Arrays.asList("shop:*", "note:*", "coupon:*", "dashboard:*"));

        return response;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 将token加入黑名单
            long expireTime = jwtUtil.getExpireTime(token);
            if (expireTime > 0) {
                redisUtil.set("merchant:token:blacklist:" + token, "1", (int) expireTime);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantLoginResponse register(String phone, String code, String password, String merchantName) {
        // 验证验证码
        String cacheCode = redisUtil.get(MERCHANT_SMS_CODE_PREFIX + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            throw new BusinessException(40002, "验证码错误或已过期");
        }

        // 检查手机号是否已注册
        MerchantUser existUser = merchantUserMapper.selectByPhone(phone);
        if (existUser != null) {
            throw new BusinessException(40001, "该手机号已注册");
        }

        // 创建商家
        Merchant merchant = new Merchant();
        merchant.setName(merchantName);
        merchant.setContactPhone(phone);
        merchant.setStatus(1);
        merchantMapper.insert(merchant);

        // 创建商家管理员账号
        MerchantUser user = new MerchantUser();
        user.setMerchantId(merchant.getId());
        user.setPhone(phone);
        user.setPassword(password); // TODO: 应使用BCrypt加密
        user.setName("管理员");
        user.setStatus(1);
        user.setLastLoginAt(LocalDateTime.now());
        merchantUserMapper.insert(user);

        // 删除验证码
        redisUtil.delete(MERCHANT_SMS_CODE_PREFIX + phone);

        return buildLoginResponse(user);
    }

    private MerchantLoginResponse buildLoginResponse(MerchantUser user) {
        String token = jwtUtil.generateToken(user.getId());

        MerchantLoginResponse response = new MerchantLoginResponse();
        response.setToken(token);
        response.setUserInfo(getCurrentUserInfo(user.getId()));

        return response;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

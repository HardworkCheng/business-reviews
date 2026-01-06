package com.businessreviews.service.impl.merchant;

import com.businessreviews.model.dto.merchant.MerchantLoginDTO;
import com.businessreviews.model.dto.merchant.MerchantRegisterDTO;
import com.businessreviews.model.vo.merchant.MerchantLoginVO;
import com.businessreviews.model.vo.merchant.MerchantUserInfoVO;
import com.businessreviews.model.dataobject.MerchantDO;
import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.model.dataobject.UserDO;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.service.merchant.MerchantAuthService;
import com.businessreviews.util.JwtUtil;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

/**
 * 商家认证服务实现类
 * <p>
 * 处理商家端的登录、注册及认证相关的业务逻辑。
 * 已整合 merchant_users 表到 merchants 表，统一使用 merchants 表进行认证管理。
 * 核心功能包括：
 * 1. 发送短信验证码（用于登录或注册）
 * 2. 商家登录（验证码登录、密码登录）
 * 3. 商家注册与入驻（同时自动创建关联的UniApp账号和默认门店）
 * 4. 商家信息更新与注销
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantAuthServiceImpl implements MerchantAuthService {

    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final ShopMapper shopMapper;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    private static final String CODE_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 发送商家端验证码
     * <p>
     * 生成6位随机验证码并存储到Redis（有效期5分钟）。
     * 包含1分钟的发送频率限制。
     * </p>
     *
     * @param phone 手机号
     */
    @Override
    public void sendCode(String phone) {
        // 检查发送频率
        String limitKey = RedisKeyConstants.MERCHANT_SMS_LIMIT + phone;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40005, "操作过于频繁，请稍后再试");
        }

        // 生成6位验证码
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARSET.charAt(new Random().nextInt(CODE_CHARSET.length())));
        }
        String code = sb.toString();

        // 存储到Redis，5分钟过期
        redisUtil.set(RedisKeyConstants.MERCHANT_SMS_CODE + phone, code, 300);
        redisUtil.set(limitKey, "1", 60);

        // TODO: 调用短信服务发送验证码
        log.info("商家端验证码已发送: phone={}, code={}", phone, code);
    }

    /**
     * 验证码登录
     * <p>
     * 验证手机号和验证码，成功后生成JWT Token。
     * </p>
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 登录结果（含Token和商家信息）
     * @throws BusinessException 如果验证失败
     */
    @Override
    public MerchantLoginVO loginByCode(String phone, String code) {
        // 验证验证码
        String cacheCode = redisUtil.get(RedisKeyConstants.MERCHANT_SMS_CODE + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            throw new BusinessException(40002, "验证码错误或已过期");
        }

        // 查找商家（直接使用merchants表）
        MerchantDO merchant = merchantMapper.selectByPhone(phone);
        if (merchant == null) {
            throw new BusinessException(40401, "账号不存在，请先入驻");
        }

        // 删除验证码
        redisUtil.delete(RedisKeyConstants.MERCHANT_SMS_CODE + phone);

        // 更新登录时间
        merchant.setLastLoginAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        // 生成Token并返回
        return buildLoginResponse(merchant);
    }

    /**
     * 密码登录
     * <p>
     * 验证手机号和加密密码。
     * </p>
     *
     * @param request 登录请求
     * @return 登录结果
     * @throws BusinessException 如果账号或密码错误
     */
    @Override
    public MerchantLoginVO loginByPassword(MerchantLoginDTO request) {
        // 查找商家（直接使用merchants表）
        MerchantDO merchant = merchantMapper.selectByPhone(request.getPhone());
        if (merchant == null) {
            throw new BusinessException(40401, "账号不存在");
        }

        // 验证密码（使用密码加密工具类）
        if (!PasswordUtil.verifyPassword(request.getPassword(), merchant.getPassword())) {
            throw new BusinessException(40003, "密码错误");
        }

        // 更新登录时间
        merchant.setLastLoginAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        return buildLoginResponse(merchant);
    }

    /**
     * 获取当前登录商家信息
     * <p>
     * 返回商家的详细资料及权限列表。
     * </p>
     *
     * @param merchantId 商家ID
     * @return 商家信息VO
     */
    @Override
    public MerchantUserInfoVO getCurrentUserInfo(Long merchantId) {
        MerchantDO merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40401, "商家不存在");
        }

        MerchantUserInfoVO response = new MerchantUserInfoVO();
        response.setUserId(merchant.getId().toString());
        response.setMerchantId(merchant.getId().toString());
        response.setMerchantName(merchant.getName());
        response.setMerchantLogo(merchant.getLogo());
        response.setName(merchant.getMerchantOwnerName());
        response.setPhone(maskPhone(merchant.getContactPhone()));
        response.setContactEmail(merchant.getContactEmail());
        response.setAvatar(merchant.getAvatar());
        response.setLicenseNo(merchant.getLicenseNo());
        response.setLicenseImage(merchant.getLicenseImage());
        response.setRoleName("管理员"); // TODO: 根据角色ID查询
        response.setPermissions(Arrays.asList("shop:*", "note:*", "coupon:*", "dashboard:*"));

        return response;
    }

    /**
     * 退出登录
     * <p>
     * 将Token加入黑名单使其失效。
     * </p>
     *
     * @param token JWT Token
     */
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

    /**
     * 商家注册入驻
     * <p>
     * 1. 验证手机验证码
     * 2. 创建商家账号(merchants)
     * 3. 自动同步创建UniApp用户端账号(users)
     * 4. 自动创建默认门店(shop)
     * </p>
     *
     * @param request 注册信息
     * @return 注册并自动登录的结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantLoginVO register(MerchantRegisterDTO request) {
        // 验证验证码
        String cacheCode = redisUtil.get(RedisKeyConstants.MERCHANT_SMS_CODE + request.getPhone());
        if (cacheCode == null || !cacheCode.equals(request.getCode())) {
            throw new BusinessException(40002, "验证码错误或已过期");
        }

        // 检查手机号是否已注册（商家表）
        int count = merchantMapper.countByPhone(request.getPhone());
        if (count > 0) {
            throw new BusinessException(40001, "该手机号已注册");
        }

        // 创建商家（整合了原merchant_users表的所有信息）
        MerchantDO merchant = new MerchantDO();

        // 基本信息
        merchant.setName(request.getMerchantName());
        merchant.setMerchantOwnerName(request.getMerchantOwnerName());
        merchant.setLogo(request.getLogo());
        merchant.setAvatar(request.getAvatar());

        // 联系人信息

        merchant.setContactPhone(request.getPhone());
        merchant.setContactEmail(request.getContactEmail());

        // 营业资质信息
        merchant.setLicenseNo(request.getLicenseNo());
        merchant.setLicenseImage(request.getLicenseImage());

        // 登录信息（使用密码加密）
        merchant.setPassword(PasswordUtil.encryptPassword(request.getPassword()));
        merchant.setLastLoginAt(LocalDateTime.now());

        // 状态
        merchant.setStatus(1); // 1正常

        merchantMapper.insert(merchant);
        log.info("商家入驻成功: merchantId={}, name={}", merchant.getId(), merchant.getName());

        // 自动在UniApp用户表(users)中创建商家账号
        createUniAppUser(merchant, request);

        // 自动创建默认门店，将商家信息注入到门店管理中
        createDefaultShop(merchant, request);

        // 删除验证码
        redisUtil.delete(RedisKeyConstants.MERCHANT_SMS_CODE + request.getPhone());

        return buildLoginResponse(merchant);
    }

    /**
     * 自动在UniApp用户表(users)中创建商家账号
     * 字段映射：
     * - users.phone = merchants.contact_phone
     * - users.username = merchants.name
     * - users.avatar = merchants.logo (或 avatar)
     * - users.password = merchants.password
     */
    private void createUniAppUser(MerchantDO merchant, MerchantRegisterDTO request) {
        // 检查手机号是否已在users表中存在
        UserDO existingUser = userMapper.selectByPhone(request.getPhone());
        if (existingUser != null) {
            log.info("UniApp用户已存在，跳过创建: phone={}", request.getPhone());
            return;
        }

        UserDO user = new UserDO();
        user.setPhone(request.getPhone());
        user.setUsername(request.getMerchantName());
        // 优先使用avatar，如果没有则使用logo
        String userAvatar = request.getAvatar() != null ? request.getAvatar() : request.getLogo();
        user.setAvatar(userAvatar);
        user.setPassword(request.getPassword());
        user.setBio(request.getMerchantName() + " 官方账号");
        user.setGender(0); // 未知
        user.setStatus(1); // 正常
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("自动创建UniApp用户成功: userId={}, phone={}, username={}", user.getId(), user.getPhone(), user.getUsername());
    }

    /**
     * 自动创建默认门店，将商家注册信息注入到门店管理中
     * 避免商家二次填写重复信息
     */
    private void createDefaultShop(MerchantDO merchant, MerchantRegisterDTO request) {
        ShopDO shop = new ShopDO();
        shop.setMerchantId(merchant.getId());
        shop.setName(request.getMerchantName()); // 商家名称 -> 门店名称
        shop.setHeaderImage(request.getLogo()); // 商家Logo -> 门店封面图
        shop.setPhone(request.getPhone()); // 联系电话
        shop.setAddress("待完善"); // 默认地址，商家后续可在门店管理中修改
        shop.setDescription(request.getMerchantName() + " 欢迎您的光临"); // 默认描述
        shop.setCategoryId(1); // 默认分类（美食）
        shop.setStatus(1); // 营业中
        shop.setRating(new BigDecimal("5.0")); // 默认评分
        shop.setTasteScore(new BigDecimal("5.0"));
        shop.setEnvironmentScore(new BigDecimal("5.0"));
        shop.setServiceScore(new BigDecimal("5.0"));
        shop.setReviewCount(0);
        shop.setPopularity(0);
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());

        shopMapper.insert(shop);
        log.info("自动创建默认门店成功: shopId={}, merchantId={}, name={}", shop.getId(), merchant.getId(), shop.getName());
    }

    private MerchantLoginVO buildLoginResponse(MerchantDO merchant) {
        // 使用商家ID生成Token
        String token = jwtUtil.generateToken(merchant.getId());

        MerchantLoginVO response = new MerchantLoginVO();
        response.setToken(token);
        response.setUserInfo(getCurrentUserInfo(merchant.getId()));

        return response;
    }

    /**
     * 更新商家资料
     * <p>
     * 更新商家的基本信息、联系方式及营业资质。
     * 同时会同步更新关联门店的名称和电话。
     * </p>
     *
     * @param merchantId 商家ID
     * @param request    更新请求
     */
    @Override
    public void updateProfile(Long merchantId, com.businessreviews.model.dto.merchant.MerchantUpdateDTO request) {
        MerchantDO merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40401, "商家不存在");
        }

        if (request.getMerchantName() != null)
            merchant.setName(request.getMerchantName());
        if (request.getMerchantOwnerName() != null)
            merchant.setMerchantOwnerName(request.getMerchantOwnerName());
        if (request.getAvatar() != null)
            merchant.setAvatar(request.getAvatar());

        if (request.getContactEmail() != null)
            merchant.setContactEmail(request.getContactEmail());
        if (request.getLicenseNo() != null)
            merchant.setLicenseNo(request.getLicenseNo());
        if (request.getLicenseImage() != null)
            merchant.setLicenseImage(request.getLicenseImage());
        // 允许修改手机号，但实际业务中通常需要验证码验证。此处直接修改。
        if (request.getPhone() != null)
            merchant.setContactPhone(request.getPhone());

        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        // 同步商家名称和联系电话到关联的门店
        if (request.getMerchantName() != null || request.getPhone() != null) {
            ShopDO shopUpdate = new ShopDO();
            boolean needUpdate = false;

            if (request.getMerchantName() != null) {
                shopUpdate.setName(request.getMerchantName());
                needUpdate = true;
            }
            if (request.getPhone() != null) {
                shopUpdate.setPhone(request.getPhone());
                needUpdate = true;
            }

            if (needUpdate) {
                com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ShopDO> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
                updateWrapper.eq(ShopDO::getMerchantId, merchantId);
                shopMapper.update(shopUpdate, updateWrapper);
                log.info("同步商家信息到门店成功: merchantId={}", merchantId);
            }
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

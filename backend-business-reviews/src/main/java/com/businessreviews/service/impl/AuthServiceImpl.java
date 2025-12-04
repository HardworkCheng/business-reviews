package com.businessreviews.service.impl;

import com.businessreviews.common.Constants;
import com.businessreviews.dto.request.LoginByCodeRequest;
import com.businessreviews.dto.request.OAuthLoginRequest;
import com.businessreviews.dto.request.SendCodeRequest;
import com.businessreviews.dto.response.LoginResponse;
import com.businessreviews.dto.response.UserInfoResponse;
import com.businessreviews.entity.User;
import com.businessreviews.entity.UserStats;
import com.businessreviews.entity.VerificationCode;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.mapper.UserStatsMapper;
import com.businessreviews.mapper.VerificationCodeMapper;
import com.businessreviews.service.AuthService;
import com.businessreviews.util.JwtUtil;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserStatsMapper userStatsMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    private final SmsUtil smsUtil;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public void sendCode(SendCodeRequest request) {
        String phone = request.getPhone();
        
        // 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }
        
        // 检查发送频率限制
        String limitKey = Constants.RedisKey.SMS_LIMIT + phone;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40005, "操作过于频繁，请稍后再试");
        }
        
        // 生成6位验证码
        String code = generateCode();
        
        // 发送短信
        smsUtil.sendCode(phone, code);
        
        // 存储到Redis
        String codeKey = Constants.RedisKey.SMS_CODE + phone;
        redisUtil.set(codeKey, code, Constants.SmsCode.EXPIRE_TIME);
        redisUtil.set(limitKey, "1", Constants.SmsCode.LIMIT_TIME);
        
        // 记录到数据库
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(request.getType());
        verificationCode.setExpireAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setUsed(false);
        verificationCodeMapper.insert(verificationCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse loginByCode(LoginByCodeRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        
        log.info("开始验证码登录，手机号: {}", phone);
        
        // 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }
        
        // 验证验证码
        String codeKey = Constants.RedisKey.SMS_CODE + phone;
        String cacheCode = redisUtil.get(codeKey);
        if (cacheCode == null || !cacheCode.equals(code)) {
            log.warn("验证码错误，手机号: {}, 输入: {}, 缓存: {}", phone, code, cacheCode);
            throw new BusinessException(40002, "验证码错误或已过期");
        }
        
        // 查找或创建用户
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            log.info("用户不存在，开始注册新用户: {}", phone);
            user = registerNewUser(phone);
        } else {
            log.info("用户已存在，用户ID: {}", user.getId());
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        int updateResult = userMapper.updateById(user);
        log.info("更新最后登录时间结果: {}", updateResult);
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId());
        log.info("生成Token成功，用户ID: {}", user.getId());
        
        // 删除验证码
        redisUtil.delete(codeKey);
        
        // 构建响应
        return buildLoginResponse(user, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse oauthLogin(OAuthLoginRequest request) {
        // 这里实现第三方登录逻辑
        // 1. 根据type调用不同的OAuth接口
        // 2. 使用code换取access_token
        // 3. 获取第三方用户信息
        // 4. 根据openid查找或创建用户
        
        // 模拟实现
        throw new BusinessException(50000, "暂不支持第三方登录");
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token != null && !token.isEmpty()) {
            // 获取Token剩余有效期
            long expireTime = jwtUtil.getExpireTime(token);
            if (expireTime > 0) {
                // 加入黑名单
                String blacklistKey = Constants.RedisKey.TOKEN_BLACKLIST + token;
                redisUtil.set(blacklistKey, "1", expireTime);
            }
        }
    }

    private User registerNewUser(String phone) {
        log.info("开始注册新用户，手机号: {}", phone);
        
        User user = new User();
        user.setPhone(phone);
        user.setUsername("用户" + phone.substring(7));
        user.setAvatar("https://example.com/default-avatar.png");
        user.setStatus(1);
        
        int insertResult = userMapper.insert(user);
        log.info("用户插入结果: {}, 用户ID: {}", insertResult, user.getId());
        
        // 创建用户统计记录
        UserStats stats = new UserStats();
        stats.setUserId(user.getId());
        stats.setFollowingCount(0);
        stats.setFollowerCount(0);
        stats.setNoteCount(0);
        stats.setLikeCount(0);
        stats.setFavoriteCount(0);
        
        int statsResult = userStatsMapper.insert(stats);
        log.info("用户统计插入结果: {}, 统计ID: {}", statsResult, stats.getId());
        
        return user;
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setUserId(user.getId().toString());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(maskPhone(user.getPhone()));
        
        response.setUserInfo(userInfo);
        return response;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}

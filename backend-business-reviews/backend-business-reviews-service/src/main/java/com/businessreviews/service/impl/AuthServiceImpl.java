package com.businessreviews.service.impl;

import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.constants.SmsCodeConstants;
import com.businessreviews.common.DefaultAvatar;
import com.businessreviews.model.dto.LoginByCodeDTO;
import com.businessreviews.model.dto.LoginByPasswordDTO;
import com.businessreviews.model.dto.OAuthLoginDTO;
import com.businessreviews.model.dto.SendCodeDTO;
import com.businessreviews.model.vo.LoginVO;
import com.businessreviews.model.vo.UserInfoVO;
import com.businessreviews.model.dataobject.UserDO;
import com.businessreviews.model.dataobject.UserStatsDO;
import com.businessreviews.model.dataobject.VerificationCodeDO;
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
    public void sendCode(SendCodeDTO request) {
        String phone = request.getPhone();
        
        // 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }
        
        // 检查发送频率限制
        String limitKey = RedisKeyConstants.SMS_LIMIT + phone;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40005, "操作过于频繁，请稍后再试");
        }
        
        // 生成6位验证码
        String code = generateCode();
        
        // 发送短信
        smsUtil.sendCode(phone, code);
        
        // 存储到Redis
        String codeKey = RedisKeyConstants.SMS_CODE + phone;
        redisUtil.set(codeKey, code, SmsCodeConstants.EXPIRE_TIME);
        redisUtil.set(limitKey, "1", SmsCodeConstants.LIMIT_TIME);
        
        // 记录到数据库
        VerificationCodeDO verificationCode = new VerificationCodeDO();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(request.getType());
        verificationCode.setExpireAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setUsed(false);
        verificationCodeMapper.insert(verificationCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByCode(LoginByCodeDTO request) {
        String phone = request.getPhone();
        String code = request.getCode();
        
        log.info("开始验证码登录，手机号: {}", phone);
        
        // 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }
        
        // 验证验证码
        String codeKey = RedisKeyConstants.SMS_CODE + phone;
        String cacheCode = redisUtil.get(codeKey);
        if (cacheCode == null || !cacheCode.equals(code)) {
            log.warn("验证码错误，手机号: {}, 输入: {}, 缓存: {}", phone, code, cacheCode);
            throw new BusinessException(40002, "验证码错误或已过期");
        }
        
        // 查找或创建用户
        UserDO user = userMapper.selectByPhone(phone);
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
        
        // 清除用户信息缓存，确保获取最新数据
        redisUtil.delete(RedisKeyConstants.USER_INFO + user.getId());
        log.info("已清除用户{}的缓存", user.getId());
        
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
    public LoginVO loginByPassword(LoginByPasswordDTO request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        
        log.info("开始密码登录，手机号: {}", phone);
        
        // 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }
        
        // 查找用户
        UserDO user = userMapper.selectByPhone(phone);
        if (user == null) {
            // 为了安全，不透露用户是否存在
            log.warn("密码登录失败，用户不存在: {}", phone);
            throw new BusinessException(40003, "手机号或密码错误");
        }
        
        // 验证密码（当前密码未加密，直接比对）
        if (!password.equals(user.getPassword())) {
            log.warn("密码登录失败，密码错误: {}", phone);
            throw new BusinessException(40003, "手机号或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(40004, "账号已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("更新最后登录时间，用户ID: {}", user.getId());
        
        // 清除用户信息缓存
        redisUtil.delete(RedisKeyConstants.USER_INFO + user.getId());
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId());
        log.info("密码登录成功，用户ID: {}", user.getId());
        
        // 构建响应
        return buildLoginResponse(user, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO oauthLogin(OAuthLoginDTO request) {
        // 第三方登录已禁用
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
                String blacklistKey = RedisKeyConstants.TOKEN_BLACKLIST + token;
                redisUtil.set(blacklistKey, "1", expireTime);
            }
        }
    }

    private UserDO registerNewUser(String phone) {
        log.info("开始注册新用户，手机号: {}", phone);
        
        UserDO user = new UserDO();
        user.setPhone(phone);
        user.setUsername("用户" + phone.substring(7));
        
        // 随机选择一个默认头像
        user.setAvatar(getRandomAvatar());
        
        user.setPassword(phone); // 新用户默认密码为手机号
        user.setStatus(1);
        
        int insertResult = userMapper.insert(user);
        log.info("用户插入结果: {}, 用户ID: {}", insertResult, user.getId());
        
        // 创建用户统计记录
        UserStatsDO stats = new UserStatsDO();
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

    private LoginVO buildLoginResponse(UserDO user, String token) {
        LoginVO response = new LoginVO();
        response.setToken(token);
        
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(user.getId().toString());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setBio(user.getBio());
        userInfo.setPhone(maskPhone(user.getPhone())); // 脱敏后的手机号
        userInfo.setFullPhone(user.getPhone()); // 完整手机号
        userInfo.setGender(user.getGender());
        userInfo.setBirthday(user.getBirthday());
        userInfo.setWechatOpenid(user.getWechatOpenid());
        userInfo.setQqOpenid(user.getQqOpenid());
        userInfo.setWeiboUid(user.getWeiboUid());
        
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
    
    /**
     * 随机获取一个默认头像 URL
     *
     * @return 头像 URL
     */
    private String getRandomAvatar() {
        String avatar = DefaultAvatar.getRandomAvatar();
        log.info("为新用户随机分配头像: {}", avatar);
        return avatar;
    }
}

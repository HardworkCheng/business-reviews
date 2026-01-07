package com.businessreviews.service.impl.app;

import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.constants.SmsCodeConstants;
import com.businessreviews.common.DefaultAvatar;
import com.businessreviews.model.dto.LoginByCodeDTO;
import com.businessreviews.model.dto.LoginByPasswordDTO;
import com.businessreviews.model.dto.app.OAuthLoginDTO;
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
import com.businessreviews.enums.UserStatus;
import com.businessreviews.service.app.AuthService;
import com.businessreviews.util.JwtUtil;
import com.businessreviews.util.RedisUtil;
import com.businessreviews.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 认证服务实现类 (用户端)
 * <p>
 * 处理C端用户的登录注册相关的核心业务逻辑。
 * 包括短信验证码发送、手机号验证码登录、密码登录以及Token管理。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 用户统计数据访问层 */
    private final UserStatsMapper userStatsMapper;
    /** 验证码记录数据访问层 */
    private final VerificationCodeMapper verificationCodeMapper;
    /** Redis工具类 */
    private final RedisUtil redisUtil;
    /** JWT工具类 */
    private final JwtUtil jwtUtil;
    /** 短信工具类 */
    private final SmsUtil smsUtil;

    /** 手机号正则校验模式 (1开头，第二位3-9，11位数字) */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    /** 安全随机数生成器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    /** 验证码字符集 */
    private static final String CODE_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 发送短信验证码
     * <p>
     * 业务流程：
     * 1. 校验手机号格式
     * 2. 检查发送频率（Redis限流）
     * 3. 生成6位验证码
     * 4. 调用短信服务发送
     * 5. 存入Redis（有效期5分钟）并记录数据库日志
     * </p>
     *
     * @param request 发送验证码请求参数（包含手机号、类型）
     */
    @Override
    public void sendCode(SendCodeDTO request) {
        String phone = request.getPhone();

        // 1. 验证手机号格式是否正确
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }

        // 2. 检查发送频率限制 (防止恶意刷短信)
        String limitKey = RedisKeyConstants.SMS_LIMIT + phone;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40005, "操作过于频繁，请稍后再试");
        }

        // 3. 生成6位随机验证码
        String code = generateCode();

        // 4. 发送短信
        smsUtil.sendCode(phone, code);

        // 5. 存储验证码到Redis
        String codeKey = RedisKeyConstants.SMS_CODE + phone;
        // 设置验证码过期时间
        redisUtil.set(codeKey, code, SmsCodeConstants.EXPIRE_TIME);
        // 设置发送频率限制时间
        redisUtil.set(limitKey, "1", SmsCodeConstants.LIMIT_TIME);

        // 6. 记录发送日志到数据库
        VerificationCodeDO verificationCode = new VerificationCodeDO();
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(request.getType());
        verificationCode.setExpireAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setUsed(false);
        verificationCodeMapper.insert(verificationCode);
    }

    /**
     * 验证码登录
     * <p>
     * 核心逻辑：
     * 1. 验证手机号和验证码
     * 2. 用户不存在则自动注册
     * 3. 生成JWT Token
     * 4. 更新登录状态并返回用户信息
     * </p>
     *
     * @param request 验证码登录请求参数
     * @return 登录成功响应（包含Token和用户信息）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByCode(LoginByCodeDTO request) {
        String phone = request.getPhone();
        String code = request.getCode();

        log.info("开始验证码登录，手机号: {}", phone);

        // 1. 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }

        // 2. 校验验证码有效性
        String codeKey = RedisKeyConstants.SMS_CODE + phone;
        String cacheCode = redisUtil.get(codeKey);
        // 验证码不存在或不匹配
        if (cacheCode == null || !cacheCode.equals(code)) {
            log.warn("验证码错误，手机号: {}, 输入: {}, 缓存: {}", phone, code, cacheCode);
            throw new BusinessException(40002, "验证码错误或已过期");
        }

        // 3. 查找用户，如果不存在则注册
        UserDO user = userMapper.selectByPhone(phone);
        if (user == null) {
            log.info("用户不存在，开始注册新用户: {}", phone);
            // 自动注册逻辑
            user = registerNewUser(phone);
        } else {
            log.info("用户已存在，用户ID: {}", user.getId());
        }

        // 4. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        int updateResult = userMapper.updateById(user);
        log.info("更新最后登录时间结果: {}", updateResult);

        // 5. 清除Redis中的用户信息缓存，确保下次获取的是最新数据
        redisUtil.delete(RedisKeyConstants.USER_INFO + user.getId());
        log.info("已清除用户{}的缓存", user.getId());

        // 6. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId());
        log.info("生成Token成功，用户ID: {}", user.getId());

        // 7. 登录成功后删除验证码，防止重复使用
        redisUtil.delete(codeKey);

        // 8. 构建并返回响应数据
        return buildLoginResponse(user, token);
    }

    /**
     * 密码登录
     * <p>
     * 核心逻辑：
     * 1. 校验账号密码是否匹配
     * 2. 检查账户状态（是否被禁用）
     * 3. 生成Token并更新登录时间
     * </p>
     *
     * @param request 密码登录请求参数
     * @return 登录成功响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByPassword(LoginByPasswordDTO request) {
        String phone = request.getPhone();
        String password = request.getPassword();

        log.info("开始密码登录，手机号: {}", phone);

        // 1. 验证手机号格式
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式错误");
        }

        // 2. 查找用户
        UserDO user = userMapper.selectByPhone(phone);
        if (user == null) {
            // 注意：为了安全，不应明确提示"用户不存在"，防止账号遍历攻击
            log.warn("密码登录失败，用户不存在: {}", phone);
            throw new BusinessException(40003, "手机号或密码错误");
        }

        // 3. 验证密码（FIXME: 生产环境应使用BCrypt等加密算法，当前比较的是明文）
        if (!password.equals(user.getPassword())) {
            log.warn("密码登录失败，密码错误: {}", phone);
            throw new BusinessException(40003, "手机号或密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(40004, "账号已被禁用");
        }

        // 5. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("更新最后登录时间，用户ID: {}", user.getId());

        // 6. 清除用户信息缓存
        redisUtil.delete(RedisKeyConstants.USER_INFO + user.getId());

        // 7. 生成Token
        String token = jwtUtil.generateToken(user.getId());
        log.info("密码登录成功，用户ID: {}", user.getId());

        // 8. 构建响应
        return buildLoginResponse(user, token);
    }

    /**
     * 第三方登录
     *
     * @param request 第三方登录请求参数
     * @return 登录结果
     * @throws BusinessException 目前抛出不支持异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO oauthLogin(OAuthLoginDTO request) {
        // FIXME: 待实现第三方登录逻辑
        // 第三方登录目前暂不支持
        throw new BusinessException(50000, "暂不支持第三方登录");
    }

    /**
     * 退出登录
     * <p>
     * 实现方式：
     * 将Token加入Redis黑名单，使其失效，直到Token自然过期
     * </p>
     *
     * @param token 用户当前的JWT Token
     */
    @Override
    public void logout(String token) {
        // 处理Token前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && !token.isEmpty()) {
            // 1. 获取Token剩余有效期
            long expireTime = jwtUtil.getExpireTime(token);
            if (expireTime > 0) {
                // 2. 将Token加入黑名单
                String blacklistKey = RedisKeyConstants.TOKEN_BLACKLIST + token;
                // Value设为1，过期时间为Token剩余时间
                redisUtil.set(blacklistKey, "1", expireTime);
            }
        }
    }

    /**
     * 注册新用户
     * <p>
     * 初始化操作：
     * 1. 创建基础用户信息（默认用户名、随机头像、默认密码）
     * 2. 创建用户统计信息（关注数、粉丝数等初始化为0）
     * </p>
     *
     * @param phone 注册手机号
     * @return 注册完成的用户对象
     */
    private UserDO registerNewUser(String phone) {
        log.info("开始注册新用户，手机号: {}", phone);

        // 1. 设置用户基础信息
        UserDO user = new UserDO();
        user.setPhone(phone);
        // 默认用户名: 用户+手机号后四位
        user.setUsername("用户" + phone.substring(7));

        // 随机选择一个默认头像
        user.setAvatar(getRandomAvatar());

        user.setPassword(phone); // FIXME: 新用户默认密码目前设为手机号，建议后续引导修改
        user.setStatus(UserStatus.NORMAL.getCode()); // 状态：正常

        int insertResult = userMapper.insert(user);
        log.info("用户插入结果: {}, 用户ID: {}", insertResult, user.getId());

        // 2. 初始化用户统计记录
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

    /**
     * 构建登录响应对象
     * <p>
     * 封装UserDO转VO的逻辑
     * </p>
     *
     * @param user  用户实体
     * @param token 认证Token
     * @return 登录VO
     */
    private LoginVO buildLoginResponse(UserDO user, String token) {
        LoginVO response = new LoginVO();
        response.setToken(token);

        // 转换用户信息VO
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(user.getId().toString());
        userInfo.setUsername(user.getUsername());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setBio(user.getBio());
        userInfo.setPhone(maskPhone(user.getPhone())); // 手机号脱敏处理
        userInfo.setGender(user.getGender());
        userInfo.setBirthday(user.getBirthday());
        userInfo.setWechatOpenid(user.getWechatOpenid());
        userInfo.setQqOpenid(user.getQqOpenid());
        userInfo.setWeiboUid(user.getWeiboUid());

        response.setUserInfo(userInfo);
        return response;
    }

    /**
     * 生成6位数字验证码
     * 
     * @return 6位随机字符串
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARSET.charAt(SECURE_RANDOM.nextInt(CODE_CHARSET.length())));
        }
        return sb.toString();
    }

    /**
     * 手机号脱敏
     * 
     * @param phone 原手机号
     * @return 脱敏后手机号 (前3位+****+后4位)
     */
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

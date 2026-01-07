package com.businessreviews.service.impl.common;

import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.mapper.UserMapper;
import com.businessreviews.model.dataobject.UserDO;
import com.businessreviews.model.vo.UserBasicVO;
import com.businessreviews.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户基础信息缓存服务
 * <p>
 * 提供用户头像、昵称等高频访问字段的 Redis 缓存能力。
 * 采用 Cache-Aside 模式，支持单个和批量查询。
 * </p>
 * <p>
 * 缓存策略：
 * - TTL: 15分钟（用户信息变更频率低，但需要保持一定实时性）
 * - 失效：用户更新资料时主动清除缓存
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final RedisUtil redisUtil;
    private final UserMapper userMapper;

    /** 缓存过期时间：15分钟 */
    private static final long CACHE_TTL_SECONDS = 900L;

    /**
     * 获取单个用户的基础信息（带缓存）
     *
     * @param userId 用户ID
     * @return 用户基础信息VO，不存在则返回null
     */
    public UserBasicVO getUserBasic(Long userId) {
        if (userId == null) {
            return null;
        }

        String cacheKey = RedisKeyConstants.USER_BASIC_INFO + userId;

        // 1. 尝试从缓存获取
        try {
            UserBasicVO cached = redisUtil.getObject(cacheKey, UserBasicVO.class);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("用户缓存读取失败: userId={}, error={}", userId, e.getMessage());
        }

        // 2. 缓存未命中，查询数据库
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        // 3. 转换并写入缓存
        UserBasicVO vo = convertToBasicVO(user);
        try {
            redisUtil.setObject(cacheKey, vo, CACHE_TTL_SECONDS);
        } catch (Exception e) {
            log.warn("用户缓存写入失败: userId={}, error={}", userId, e.getMessage());
        }

        return vo;
    }

    /**
     * 批量获取用户基础信息（带缓存）
     * <p>
     * 优化策略：
     * 1. 批量从 Redis 获取已缓存的用户
     * 2. 对未命中的用户ID进行批量数据库查询
     * 3. 将新查询的数据写入缓存
     * </p>
     *
     * @param userIds 用户ID集合
     * @return 用户ID到基础信息的映射
     */
    public Map<Long, UserBasicVO> batchGetUserBasic(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 去重
        Set<Long> uniqueIds = new HashSet<>(userIds);
        Map<Long, UserBasicVO> result = new HashMap<>();

        // 1. 构建缓存Key列表
        List<Long> idList = new ArrayList<>(uniqueIds);
        List<String> cacheKeys = idList.stream()
                .map(id -> RedisKeyConstants.USER_BASIC_INFO + id)
                .collect(Collectors.toList());

        // 2. 批量从Redis获取
        List<Long> missedIds = new ArrayList<>();
        try {
            List<UserBasicVO> cachedList = redisUtil.multiGetObjects(cacheKeys, UserBasicVO.class);
            for (int i = 0; i < idList.size(); i++) {
                Long userId = idList.get(i);
                UserBasicVO cached = cachedList.get(i);
                if (cached != null) {
                    result.put(userId, cached);
                } else {
                    missedIds.add(userId);
                }
            }

            if (!missedIds.isEmpty()) {
                log.debug("用户缓存批量查询: 总数={}, 命中={}, 未命中={}",
                        idList.size(), result.size(), missedIds.size());
            }
        } catch (Exception e) {
            log.warn("用户缓存批量读取失败: {}", e.getMessage());
            missedIds = idList; // 缓存失败，全部从数据库查
        }

        // 3. 对未命中的ID进行数据库批量查询
        if (!missedIds.isEmpty()) {
            List<UserDO> users = userMapper.selectBatchIds(missedIds);

            for (UserDO user : users) {
                UserBasicVO vo = convertToBasicVO(user);
                result.put(user.getId(), vo);

                // 异步写入缓存（这里简单同步写入，生产环境可考虑异步）
                try {
                    String cacheKey = RedisKeyConstants.USER_BASIC_INFO + user.getId();
                    redisUtil.setObject(cacheKey, vo, CACHE_TTL_SECONDS);
                } catch (Exception e) {
                    log.warn("用户缓存写入失败: userId={}", user.getId());
                }
            }
        }

        return result;
    }

    /**
     * 清除用户缓存（用户更新资料时调用）
     *
     * @param userId 用户ID
     */
    public void evictUserCache(Long userId) {
        if (userId == null) {
            return;
        }

        String cacheKey = RedisKeyConstants.USER_BASIC_INFO + userId;
        try {
            redisUtil.delete(cacheKey);
            log.info("清除用户缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 预热用户缓存（启动时或批量操作时调用）
     *
     * @param userIds 用户ID列表
     */
    public void warmUpCache(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        List<UserDO> users = userMapper.selectBatchIds(userIds);
        for (UserDO user : users) {
            try {
                String cacheKey = RedisKeyConstants.USER_BASIC_INFO + user.getId();
                UserBasicVO vo = convertToBasicVO(user);
                redisUtil.setObject(cacheKey, vo, CACHE_TTL_SECONDS);
            } catch (Exception e) {
                log.warn("缓存预热失败: userId={}", user.getId());
            }
        }

        log.info("用户缓存预热完成: count={}", users.size());
    }

    private UserBasicVO convertToBasicVO(UserDO user) {
        UserBasicVO vo = new UserBasicVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}

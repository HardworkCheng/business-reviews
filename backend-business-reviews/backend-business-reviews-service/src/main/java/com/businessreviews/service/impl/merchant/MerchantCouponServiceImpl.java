package com.businessreviews.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.businessreviews.common.PageResult;
import com.businessreviews.model.dto.merchant.CreateCouponDTO;
import com.businessreviews.model.vo.CouponDetailVO;
import com.businessreviews.model.vo.CouponItemVO;
import com.businessreviews.model.dataobject.CouponDO;
import com.businessreviews.model.dataobject.MerchantDO;
import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.model.dataobject.UserCouponDO;
import com.businessreviews.exception.BusinessException;
import com.businessreviews.mapper.CouponMapper;
import com.businessreviews.mapper.MerchantMapper;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.mapper.UserCouponMapper;
import com.businessreviews.service.merchant.MerchantCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商家优惠券服务实现类
 * <p>
 * 处理商家优惠券的全生命周期管理。
 * 核心功能包括：
 * 1. 优惠券的创建、修改与状态管理（支持满减、折扣、代金券）
 * 2. 优惠券列表与详情查询
 * 3. 优惠券数据统计（发放量、领取量、核销量、转化率）
 * 4. 线下核销与验券功能（扫码核销）
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCouponServiceImpl implements MerchantCouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final MerchantMapper merchantMapper;
    private final ShopMapper shopMapper;

    /**
     * 获取优惠券列表
     * <p>
     * 支持多条件组合筛选。
     * </p>
     *
     * @param merchantId 商家ID
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @param type       优惠券类型 (1:满减, 2:折扣, 3:代金)
     * @param status     状态 (1:启用, 2:停用, 3:结束)
     * @param shopId     适用门店ID
     * @return 优惠券VO分页列表
     */
    @Override
    public PageResult<CouponItemVO> getCouponList(Long merchantId, Integer pageNum, Integer pageSize,
            Integer type, Integer status, Long shopId) {
        log.info("获取优惠券列表: merchantId={}, pageNum={}, pageSize={}, type={}, status={}",
                merchantId, pageNum, pageSize, type, status);

        // 验证商家
        validateMerchant(merchantId);

        // 构建查询条件
        LambdaQueryWrapper<CouponDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CouponDO::getMerchantId, merchantId);

        if (type != null) {
            wrapper.eq(CouponDO::getType, type);
        }
        if (status != null) {
            wrapper.eq(CouponDO::getStatus, status);
        }
        if (shopId != null) {
            wrapper.eq(CouponDO::getShopId, shopId);
        }
        wrapper.orderByDesc(CouponDO::getCreatedAt);

        // 分页查询
        Page<CouponDO> page = new Page<>(pageNum, pageSize);
        Page<CouponDO> couponPage = couponMapper.selectPage(page, wrapper);

        // 转换结果
        List<CouponItemVO> list = couponPage.getRecords().stream()
                .map(this::convertToCouponItemVO)
                .collect(Collectors.toList());

        PageResult<CouponItemVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(couponPage.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 获取优惠券详情
     *
     * @param merchantId 商家ID
     * @param couponId   优惠券ID
     * @return 优惠券详情VO
     * @throws BusinessException 如果不存在或无权访问
     */
    @Override
    public CouponDetailVO getCouponDetail(Long merchantId, Long couponId) {
        log.info("获取优惠券详情: merchantId={}, couponId={}", merchantId, couponId);

        // 验证商家
        validateMerchant(merchantId);

        // 查询优惠券
        CouponDO coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }

        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权访问此优惠券");
        }

        return convertToCouponDetailVO(coupon);
    }

    /**
     * 创建优惠券
     * <p>
     * 创建新的优惠券活动，包括类型校验、金额校验等。
     * </p>
     *
     * @param merchantId 商家ID
     * @param operatorId 操作人ID
     * @param request    创建参数
     * @return 新创建的优惠券ID
     * @throws BusinessException 如果参数校验失败
     */
    @Override
    @Transactional
    public Long createCoupon(Long merchantId, Long operatorId, CreateCouponDTO request) {
        log.info("创建优惠券: merchantId={}, operatorId={}, type={}", merchantId, operatorId, request.getType());

        // 验证商家
        validateMerchant(merchantId);

        // 验证优惠券类型和必填字段
        validateCouponTypeFields(request);

        // 创建优惠券
        CouponDO coupon = new CouponDO();
        coupon.setMerchantId(merchantId);
        coupon.setTitle(request.getTitle());
        coupon.setDescription(request.getDescription());
        coupon.setType(request.getType());

        // 根据类型设置金额或折扣
        // type=1 满减券：需要 amount 和 minAmount
        // type=2 折扣券：需要 discount 和 minAmount
        // type=3 代金券：需要 amount，minAmount=0
        if (request.getType() == 1) {
            // 满减券
            coupon.setAmount(request.getAmount());
            coupon.setMinAmount(request.getMinAmount() != null ? request.getMinAmount() : BigDecimal.ZERO);
            coupon.setDiscount(null);
        } else if (request.getType() == 2) {
            // 折扣券
            coupon.setDiscount(request.getDiscount());
            coupon.setMinAmount(request.getMinAmount() != null ? request.getMinAmount() : BigDecimal.ZERO);
            coupon.setAmount(null);
        } else if (request.getType() == 3) {
            // 代金券
            coupon.setAmount(request.getAmount());
            coupon.setMinAmount(BigDecimal.ZERO);
            coupon.setDiscount(null);
        }

        // 设置适用门店（null表示全部门店可用）
        coupon.setShopId(request.getShopId());

        // 设置数量：remainCount 初始化为 totalCount
        coupon.setTotalCount(request.getTotalCount());
        coupon.setRemainCount(request.getTotalCount());
        coupon.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
        coupon.setStackable(request.getStackable() != null && request.getStackable() ? 1 : 0);

        // 设置时间
        coupon.setStartTime(request.getStartTime());
        coupon.setEndTime(request.getEndTime());
        coupon.setUseStartTime(request.getStartTime());
        coupon.setUseEndTime(request.getEndTime());

        coupon.setStatus(1); // 默认启用
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUpdatedAt(LocalDateTime.now());

        couponMapper.insert(coupon);
        log.info("优惠券创建成功: couponId={}, title={}", coupon.getId(), coupon.getTitle());

        return coupon.getId();
    }

    /**
     * 验证优惠券类型和必填字段
     */
    private void validateCouponTypeFields(CreateCouponDTO request) {
        Integer type = request.getType();
        if (type == null || type < 1 || type > 3) {
            throw new BusinessException(40001, "无效的优惠券类型，支持：1满减券，2折扣券，3代金券");
        }

        if (type == 1) {
            // 满减券需要 amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(40002, "满减券的优惠金额必须大于0");
            }
        } else if (type == 2) {
            // 折扣券需要 discount
            if (request.getDiscount() == null ||
                    request.getDiscount().compareTo(BigDecimal.ZERO) <= 0 ||
                    request.getDiscount().compareTo(BigDecimal.ONE) >= 0) {
                throw new BusinessException(40003, "折扣券的折扣必须在0.01-0.99之间");
            }
        } else if (type == 3) {
            // 代金券需要 amount
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(40004, "代金券的金额必须大于0");
            }
        }
    }

    /**
     * 更新优惠券
     * <p>
     * 更新优惠券信息。
     * 注意：部分敏感字段（如金额、类型）在活动开始后可能受限（当前实现较宽松）。
     * </p>
     *
     * @param merchantId 商家ID
     * @param operatorId 操作人ID
     * @param couponId   优惠券ID
     * @param request    更新参数
     */
    @Override
    @Transactional
    public void updateCoupon(Long merchantId, Long operatorId, Long couponId, CreateCouponDTO request) {
        log.info("更新优惠券: merchantId={}, couponId={}, type={}", merchantId, couponId, request.getType());

        // 验证商家
        validateMerchant(merchantId);

        // 查询优惠券
        CouponDO coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }

        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权修改此优惠券");
        }

        // 验证优惠券类型和必填字段
        if (request.getType() != null) {
            validateCouponTypeFields(request);
        }

        // 更新优惠券信息
        if (request.getTitle() != null) {
            coupon.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            coupon.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            coupon.setType(request.getType());

            // 根据类型更新对应字段
            if (request.getType() == 1) {
                // 满减券
                coupon.setAmount(request.getAmount());
                coupon.setMinAmount(request.getMinAmount() != null ? request.getMinAmount() : BigDecimal.ZERO);
                coupon.setDiscount(null);
            } else if (request.getType() == 2) {
                // 折扣券
                coupon.setDiscount(request.getDiscount());
                coupon.setMinAmount(request.getMinAmount() != null ? request.getMinAmount() : BigDecimal.ZERO);
                coupon.setAmount(null);
            } else if (request.getType() == 3) {
                // 代金券
                coupon.setAmount(request.getAmount());
                coupon.setMinAmount(BigDecimal.ZERO);
                coupon.setDiscount(null);
            }
        } else {
            // 类型未变，单独更新字段
            if (request.getAmount() != null) {
                coupon.setAmount(request.getAmount());
            }
            if (request.getDiscount() != null) {
                coupon.setDiscount(request.getDiscount());
            }
            if (request.getMinAmount() != null) {
                coupon.setMinAmount(request.getMinAmount());
            }
        }

        // 设置适用门店
        if (request.getShopId() != null) {
            coupon.setShopId(request.getShopId());
        }

        if (request.getTotalCount() != null) {
            int diff = request.getTotalCount() - coupon.getTotalCount();
            coupon.setTotalCount(request.getTotalCount());
            coupon.setRemainCount(coupon.getRemainCount() + diff);
        }
        if (request.getPerUserLimit() != null) {
            coupon.setPerUserLimit(request.getPerUserLimit());
        }
        if (request.getStartTime() != null) {
            coupon.setStartTime(request.getStartTime());
            coupon.setUseStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            coupon.setEndTime(request.getEndTime());
            coupon.setUseEndTime(request.getEndTime());
        }
        if (request.getStackable() != null) {
            coupon.setStackable(request.getStackable() ? 1 : 0);
        }

        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);
        log.info("优惠券更新成功: couponId={}", couponId);
    }

    /**
     * 更新优惠券状态
     * <p>
     * 上架或下架优惠券。
     * </p>
     *
     * @param merchantId 商家ID
     * @param operatorId 操作人ID
     * @param couponId   优惠券ID
     * @param status     新状态
     */
    @Override
    @Transactional
    public void updateCouponStatus(Long merchantId, Long operatorId, Long couponId, Integer status) {
        log.info("更新优惠券状态: merchantId={}, couponId={}, status={}", merchantId, couponId, status);

        // 验证商家
        validateMerchant(merchantId);

        // 查询优惠券
        CouponDO coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }

        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权修改此优惠券");
        }

        // 更新状态
        coupon.setStatus(status);
        coupon.setUpdatedAt(LocalDateTime.now());
        couponMapper.updateById(coupon);
        log.info("优惠券状态更新成功: couponId={}, status={}", couponId, status);
    }

    /**
     * 获取优惠券统计数据
     * <p>
     * 统计发放、领取、使用及转化率数据。
     * </p>
     *
     * @param merchantId 商家ID
     * @param couponId   优惠券ID
     * @return 统计数据Map
     */
    @Override
    public Map<String, Object> getCouponStats(Long merchantId, Long couponId) {
        log.info("获取优惠券统计: merchantId={}, couponId={}", merchantId, couponId);

        // 验证商家
        validateMerchant(merchantId);

        // 查询优惠券
        CouponDO coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException(40404, "优惠券不存在");
        }

        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权访问此优惠券");
        }

        // 统计领取数量
        LambdaQueryWrapper<UserCouponDO> claimedWrapper = new LambdaQueryWrapper<>();
        claimedWrapper.eq(UserCouponDO::getCouponId, couponId);
        Long totalClaimed = userCouponMapper.selectCount(claimedWrapper);

        // 统计已使用数量
        LambdaQueryWrapper<UserCouponDO> usedWrapper = new LambdaQueryWrapper<>();
        usedWrapper.eq(UserCouponDO::getCouponId, couponId)
                .eq(UserCouponDO::getStatus, 2); // 已使用
        Long totalUsed = userCouponMapper.selectCount(usedWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIssued", coupon.getTotalCount());
        stats.put("totalClaimed", totalClaimed);
        stats.put("totalRedeemed", totalUsed);
        stats.put("remainCount", coupon.getRemainCount());
        stats.put("claimRate", coupon.getTotalCount() > 0 ? (double) totalClaimed / coupon.getTotalCount() * 100 : 0.0);
        stats.put("redeemRate", totalClaimed > 0 ? (double) totalUsed / totalClaimed * 100 : 0.0);

        return stats;
    }

    /**
     * 核销优惠券
     * <p>
     * 商家扫描用户券码进行核销。
     * 需验证券码有效性、归属门店及有效期。
     * </p>
     *
     * @param merchantId 商家ID
     * @param operatorId 操作人ID
     * @param couponId   优惠券ID（可选，主要通过code定位）
     * @param code       券码
     * @param shopId     核销门店ID
     * @throws BusinessException 如果券码无效或已过期
     */
    @Override
    @Transactional
    public void redeemCoupon(Long merchantId, Long operatorId, Long couponId, String code, Long shopId) {
        log.info("核销优惠券: merchantId={}, code={}, shopId={}", merchantId, code, shopId);

        // 验证商家
        validateMerchant(merchantId);

        // 根据券码查询用户优惠券
        UserCouponDO userCoupon = userCouponMapper.selectByCode(code);
        if (userCoupon == null) {
            throw new BusinessException(40404, "券码无效");
        }

        // 验证优惠券归属
        CouponDO coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null || !coupon.getMerchantId().equals(merchantId)) {
            throw new BusinessException(40300, "无权核销此优惠券");
        }

        // 验证优惠券状态
        if (userCoupon.getStatus() == 2) {
            throw new BusinessException(40001, "优惠券已被使用");
        }
        if (userCoupon.getStatus() == 3) {
            throw new BusinessException(40002, "优惠券已过期");
        }

        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getUseStartTime() != null && now.isBefore(coupon.getUseStartTime())) {
            throw new BusinessException(40003, "优惠券尚未生效");
        }
        if (coupon.getUseEndTime() != null && now.isAfter(coupon.getUseEndTime())) {
            throw new BusinessException(40004, "优惠券已过期");
        }

        // 核销优惠券
        userCoupon.setStatus(2); // 已使用
        userCoupon.setUseTime(now);
        userCoupon.setUseShopId(shopId);
        userCoupon.setOperatorId(operatorId);
        userCouponMapper.updateById(userCoupon);

        log.info("优惠券核销成功: code={}", code);
    }

    /**
     * 验证券码
     * <p>
     * 查询券码对应的优惠券信息及状态，用于核销前的预览。
     * </p>
     *
     * @param merchantId 商家ID
     * @param code       券码
     * @return 验证结果（valid, message, coupon info）
     */
    @Override
    public Map<String, Object> verifyCoupon(Long merchantId, String code) {
        log.info("验证券码: merchantId={}, code={}", merchantId, code);

        Map<String, Object> result = new HashMap<>();

        // 根据券码查询用户优惠券
        UserCouponDO userCoupon = userCouponMapper.selectByCode(code);
        if (userCoupon == null) {
            result.put("valid", false);
            result.put("message", "券码无效");
            return result;
        }

        // 查询优惠券信息
        CouponDO coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            result.put("valid", false);
            result.put("message", "优惠券不存在");
            return result;
        }

        // 验证归属
        if (!coupon.getMerchantId().equals(merchantId)) {
            result.put("valid", false);
            result.put("message", "此优惠券不属于当前商家");
            return result;
        }

        // 验证状态
        if (userCoupon.getStatus() == 2) {
            result.put("valid", false);
            result.put("message", "优惠券已被使用");
            return result;
        }
        if (userCoupon.getStatus() == 3) {
            result.put("valid", false);
            result.put("message", "优惠券已过期");
            return result;
        }

        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getUseEndTime() != null && now.isAfter(coupon.getUseEndTime())) {
            result.put("valid", false);
            result.put("message", "优惠券已过期");
            return result;
        }

        // 返回优惠券信息
        result.put("valid", true);
        result.put("message", "券码有效");
        result.put("couponId", coupon.getId());
        result.put("title", coupon.getTitle());
        result.put("type", coupon.getType());
        result.put("amount", coupon.getAmount());
        result.put("discount", coupon.getDiscount());
        result.put("minAmount", coupon.getMinAmount());
        result.put("endTime", coupon.getUseEndTime());

        return result;
    }

    /**
     * 验证商家是否存在
     */
    private void validateMerchant(Long merchantId) {
        MerchantDO merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(40404, "商家不存在");
        }
        if (merchant.getStatus() != 1) {
            throw new BusinessException(40300, "商家账号已被禁用");
        }
    }

    /**
     * 转换为优惠券列表项响应
     */
    private CouponItemVO convertToCouponItemVO(CouponDO coupon) {
        CouponItemVO response = new CouponItemVO();
        response.setId(coupon.getId().toString());
        response.setType(coupon.getType());
        response.setTypeName(getCouponTypeName(coupon.getType()));
        response.setTitle(coupon.getTitle());
        response.setAmount(coupon.getAmount());
        response.setDiscount(coupon.getDiscount());
        response.setMinAmount(coupon.getMinAmount());
        response.setTotalCount(coupon.getTotalCount());
        response.setRemainCount(coupon.getRemainCount());
        response.setClaimedCount(coupon.getTotalCount() - coupon.getRemainCount());
        response.setStatus(coupon.getStatus());
        response.setStatusName(getCouponStatusName(coupon.getStatus()));
        response.setStartTime(coupon.getStartTime() != null ? coupon.getStartTime().toString() : null);
        response.setEndTime(coupon.getEndTime() != null ? coupon.getEndTime().toString() : null);
        response.setCreatedAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().toString() : null);

        // 获取门店名称
        if (coupon.getShopId() != null) {
            ShopDO shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                response.setShopName(shop.getName());
            }
        }

        return response;
    }

    /**
     * 转换为优惠券详情响应
     */
    private CouponDetailVO convertToCouponDetailVO(CouponDO coupon) {
        CouponDetailVO response = new CouponDetailVO();
        response.setId(coupon.getId().toString());
        response.setType(coupon.getType());
        response.setTypeName(getCouponTypeName(coupon.getType()));
        response.setTitle(coupon.getTitle());
        response.setDescription(coupon.getDescription());
        response.setAmount(coupon.getAmount());
        response.setDiscount(coupon.getDiscount());
        response.setMinAmount(coupon.getMinAmount());
        response.setTotalCount(coupon.getTotalCount());
        response.setRemainCount(coupon.getRemainCount());
        response.setClaimedCount(coupon.getTotalCount() - coupon.getRemainCount());
        response.setDailyLimit(coupon.getDailyLimit());
        response.setPerUserLimit(coupon.getPerUserLimit());
        response.setShopId(coupon.getShopId() != null ? coupon.getShopId().toString() : null);
        response.setStackable(coupon.getStackable() != null && coupon.getStackable() == 1);
        response.setStatus(coupon.getStatus());
        response.setStatusName(getCouponStatusName(coupon.getStatus()));

        // 格式化时间为 "yyyy-MM-dd HH:mm:ss" 格式
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");
        response.setStartTime(coupon.getStartTime() != null ? coupon.getStartTime().format(formatter) : null);
        response.setEndTime(coupon.getEndTime() != null ? coupon.getEndTime().format(formatter) : null);
        response.setUseStartTime(coupon.getUseStartTime() != null ? coupon.getUseStartTime().format(formatter) : null);
        response.setUseEndTime(coupon.getUseEndTime() != null ? coupon.getUseEndTime().format(formatter) : null);
        response.setCreatedAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().format(formatter) : null);

        // 获取门店名称
        if (coupon.getShopId() != null) {
            ShopDO shop = shopMapper.selectById(coupon.getShopId());
            if (shop != null) {
                response.setShopName(shop.getName());
            }
        }

        return response;
    }

    /**
     * 获取优惠券类型名称
     */
    private String getCouponTypeName(Integer type) {
        if (type == null)
            return "未知";
        switch (type) {
            case 1:
                return "满减券";
            case 2:
                return "折扣券";
            case 3:
                return "代金券";
            default:
                return "未知";
        }
    }

    /**
     * 获取优惠券状态名称
     */
    private String getCouponStatusName(Integer status) {
        if (status == null)
            return "未知";
        switch (status) {
            case 1:
                return "启用";
            case 2:
                return "停用";
            case 3:
                return "已结束";
            default:
                return "未知";
        }
    }
}

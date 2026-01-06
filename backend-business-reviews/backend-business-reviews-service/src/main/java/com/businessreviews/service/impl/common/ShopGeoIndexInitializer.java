package com.businessreviews.service.impl.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.mapper.ShopMapper;
import com.businessreviews.model.dataobject.ShopDO;
import com.businessreviews.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家地理位置 GEO 索引初始化器
 * <p>
 * 在应用启动完成后，将所有商家的经纬度数据加载到 Redis GEO 索引中，
 * 以支持高性能的"附近商家"搜索功能。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShopGeoIndexInitializer {

    private final ShopMapper shopMapper;
    private final RedisUtil redisUtil;

    /**
     * 应用启动完成后初始化 GEO 索引
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initShopGeoIndex() {
        log.info("开始初始化商家 GEO 索引...");
        try {
            // 查询所有状态正常且有经纬度的商家
            LambdaQueryWrapper<ShopDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShopDO::getStatus, 1)
                    .isNotNull(ShopDO::getLatitude)
                    .isNotNull(ShopDO::getLongitude);
            List<ShopDO> shops = shopMapper.selectList(wrapper);

            if (shops.isEmpty()) {
                log.info("没有需要索引的商家数据");
                return;
            }

            // 构建 GEO 位置映射
            Map<String, Point> locations = new HashMap<>();
            for (ShopDO shop : shops) {
                if (shop.getLongitude() != null && shop.getLatitude() != null) {
                    locations.put(
                            shop.getId().toString(),
                            new Point(shop.getLongitude().doubleValue(), shop.getLatitude().doubleValue()));
                }
            }

            // 批量添加到 Redis GEO
            Long added = redisUtil.geoAddAll(RedisKeyConstants.SHOP_GEO, locations);
            log.info("商家 GEO 索引初始化完成，共索引 {} 个商家", added);

        } catch (Exception e) {
            log.error("商家 GEO 索引初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 更新单个商家的 GEO 位置（当商家信息变更时调用）
     */
    public void updateShopGeo(Long shopId, Double longitude, Double latitude) {
        if (shopId == null || longitude == null || latitude == null) {
            return;
        }
        try {
            redisUtil.geoAdd(RedisKeyConstants.SHOP_GEO, longitude, latitude, shopId.toString());
            log.info("更新商家 GEO 位置: shopId={}", shopId);
        } catch (Exception e) {
            log.error("更新商家 GEO 位置失败: shopId={}, error={}", shopId, e.getMessage());
        }
    }

    /**
     * 删除商家的 GEO 位置（当商家下架时调用）
     */
    public void removeShopGeo(Long shopId) {
        if (shopId == null) {
            return;
        }
        try {
            redisUtil.geoRemove(RedisKeyConstants.SHOP_GEO, shopId.toString());
            log.info("删除商家 GEO 位置: shopId={}", shopId);
        } catch (Exception e) {
            log.error("删除商家 GEO 位置失败: shopId={}, error={}", shopId, e.getMessage());
        }
    }
}

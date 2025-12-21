package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.ShopDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商家Mapper接口
 * 合并自 ShopMapper 和 ShopDOMapper
 * 
 * @author businessreviews
 */
@Mapper
public interface ShopMapper extends BaseMapper<ShopDO> {
    
    @Update("UPDATE shops SET favorite_count = favorite_count + 1 WHERE id = #{id}")
    int incrementFavoriteCount(@Param("id") Long id);
    
    @Update("UPDATE shops SET favorite_count = favorite_count - 1 WHERE id = #{id} AND favorite_count > 0")
    int decrementFavoriteCount(@Param("id") Long id);
    
    /**
     * 根据商家ID查询门店列表
     * 
     * @param merchantId 商家ID
     * @return 门店DO列表
     */
    @Select("SELECT * FROM shops WHERE merchant_id = #{merchantId}")
    List<ShopDO> listByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据分类ID查询门店列表
     * 
     * @param categoryId 分类ID
     * @return 门店DO列表
     */
    @Select("SELECT * FROM shops WHERE category_id = #{categoryId} AND status = 1")
    List<ShopDO> listByCategoryId(@Param("categoryId") Integer categoryId);
}

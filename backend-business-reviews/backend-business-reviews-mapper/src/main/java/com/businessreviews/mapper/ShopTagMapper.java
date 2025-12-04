package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.ShopTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopTagMapper extends BaseMapper<ShopTag> {
    
    @Select("SELECT tag_name FROM shop_tags WHERE shop_id = #{shopId}")
    List<String> selectTagNamesByShopId(@Param("shopId") Long shopId);
}

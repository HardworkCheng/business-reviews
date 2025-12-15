package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家Mapper接口
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    
}

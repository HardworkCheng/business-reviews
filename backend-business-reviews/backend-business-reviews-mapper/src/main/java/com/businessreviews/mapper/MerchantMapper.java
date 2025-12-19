package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商家Mapper接口
 */
@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
    
    /**
     * 根据手机号查询商家（用于登录）
     */
    @Select("SELECT * FROM merchants WHERE contact_phone = #{phone} AND status = 1")
    Merchant selectByPhone(@Param("phone") String phone);
    
    /**
     * 检查手机号是否已存在
     */
    @Select("SELECT COUNT(*) FROM merchants WHERE contact_phone = #{phone}")
    int countByPhone(@Param("phone") String phone);
}

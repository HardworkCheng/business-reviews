package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.MerchantUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商家用户Mapper接口
 */
@Mapper
public interface MerchantUserMapper extends BaseMapper<MerchantUser> {
    
    @Select("SELECT * FROM merchant_users WHERE phone = #{phone} AND status = 1")
    MerchantUser selectByPhone(@Param("phone") String phone);
}

package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.MerchantUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商家用户Mapper接口
 */
@Mapper
public interface MerchantUserMapper extends BaseMapper<MerchantUserDO> {
    
    @Select("SELECT * FROM merchant_users WHERE phone = #{phone} AND status = 1")
    MerchantUserDO selectByPhone(@Param("phone") String phone);
}

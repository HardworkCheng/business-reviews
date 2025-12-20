package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.MerchantDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商家Mapper接口
 */
@Mapper
public interface MerchantMapper extends BaseMapper<MerchantDO> {
    
    /**
     * 根据手机号查询商家（用于登录）
     */
    @Select("SELECT * FROM merchants WHERE contact_phone = #{phone} AND status = 1")
    MerchantDO selectByPhone(@Param("phone") String phone);
    
    /**
     * 检查手机号是否已存在
     */
    @Select("SELECT COUNT(*) FROM merchants WHERE contact_phone = #{phone}")
    int countByPhone(@Param("phone") String phone);
}

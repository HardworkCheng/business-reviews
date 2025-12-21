package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 * 合并自 UserMapper 和 UserDOMapper
 * 
 * @author businessreviews
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    
    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户DO
     */
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    UserDO selectByPhone(@Param("phone") String phone);
}

package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.NotificationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationDO> {
    
    @Update("UPDATE notifications SET is_read = 1 " +
            "WHERE user_id = #{userId} AND (#{type} IS NULL OR type = #{type}) AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId, @Param("type") Integer type);
}

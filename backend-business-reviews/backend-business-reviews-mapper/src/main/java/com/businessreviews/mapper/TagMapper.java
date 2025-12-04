package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    @Update("UPDATE tags SET use_count = use_count + 1 WHERE id = #{id}")
    int incrementUseCount(@Param("id") Long id);
}

package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.SearchHistoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistoryDO> {
    
    @Select("SELECT keyword FROM search_history " +
            "GROUP BY keyword " +
            "ORDER BY SUM(search_count) DESC " +
            "LIMIT #{limit}")
    List<String> selectHotKeywords(@Param("limit") int limit);
}

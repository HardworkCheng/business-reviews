package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.TopicDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TopicMapper extends BaseMapper<TopicDO> {
    
    /**
     * 增加话题的笔记数量
     */
    @Update("UPDATE topics SET note_count = note_count + 1 WHERE id = #{topicId}")
    void incrementNoteCount(Long topicId);
    
    /**
     * 减少话题的笔记数量
     */
    @Update("UPDATE topics SET note_count = note_count - 1 WHERE id = #{topicId} AND note_count > 0")
    void decrementNoteCount(Long topicId);
}

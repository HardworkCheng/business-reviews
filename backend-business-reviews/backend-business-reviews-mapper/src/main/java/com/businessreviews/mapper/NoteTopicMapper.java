package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.NoteTopicDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NoteTopicMapper extends BaseMapper<NoteTopicDO> {

    @org.apache.ibatis.annotations.Insert("<script>" +
            "INSERT INTO note_topics (note_id, topic_id, created_at) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.noteId}, #{item.topicId}, NOW())" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<NoteTopicDO> list);
}

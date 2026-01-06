package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.model.dataobject.NoteTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTagDO> {

    @Select("SELECT tag_name FROM note_tags WHERE note_id = #{noteId}")
    List<String> selectTagNamesByNoteId(@Param("noteId") Long noteId);

    @org.apache.ibatis.annotations.Insert("<script>" +
            "INSERT INTO note_tags (note_id, tag_name, created_at) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.noteId}, #{item.tagName}, NOW())" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<NoteTagDO> list);
}

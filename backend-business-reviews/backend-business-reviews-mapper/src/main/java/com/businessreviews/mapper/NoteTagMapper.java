package com.businessreviews.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.businessreviews.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
    
    @Select("SELECT tag_name FROM note_tags WHERE note_id = #{noteId}")
    List<String> selectTagNamesByNoteId(@Param("noteId") Long noteId);
}

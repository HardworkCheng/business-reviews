package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记标签关联数据映射对象
 * <p>
 * 对应数据库表 note_tags，记录笔记与标签的关联关系
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("note_tags")
public class NoteTagDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 标签名称
     */
    private String tagName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

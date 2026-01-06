package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 话题数据映射对象
 * <p>
 * 对应数据库表 topics，存储话题信息及热度统计
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("topics")
public class TopicDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer noteCount;

    private Integer viewCount;

    /**
     * 是否热门（0否，1是）
     */
    @TableField("is_hot")
    private Integer hot;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

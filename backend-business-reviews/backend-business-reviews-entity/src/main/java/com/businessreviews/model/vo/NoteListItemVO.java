package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记列表项展示对象
 * <p>
 * 用于后台管理或其他列表视图，展示笔记核心元数据
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class NoteListItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String image;

    private String title;

    private String author;

    private String authorAvatar;

    private Long authorId;

    private Integer likes;

    private Integer views;

    private String tag;

    private String tagClass;

    private LocalDateTime createdAt;
}

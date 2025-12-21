package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分类VO
 */
@Data
public class CategoryVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String icon;
    private Integer sort;
}

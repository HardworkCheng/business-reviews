package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class CategoryResponse {
    private String id;
    private String name;
    private String icon;
    private Integer sort;
}

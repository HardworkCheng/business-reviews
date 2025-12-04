package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class UnreadCountResponse {
    private Integer total;
    private Integer messageCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer followCount;
    private Integer systemCount;
}

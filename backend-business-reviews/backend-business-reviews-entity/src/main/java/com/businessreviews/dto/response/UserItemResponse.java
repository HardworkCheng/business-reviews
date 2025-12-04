package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class UserItemResponse {
    private String userId;
    private String username;
    private String avatar;
    private String bio;
}

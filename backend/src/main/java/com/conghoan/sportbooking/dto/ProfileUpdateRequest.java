package com.conghoan.sportbooking.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String email;
    private String phone;
}

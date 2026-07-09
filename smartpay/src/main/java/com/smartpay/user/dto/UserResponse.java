package com.smartpay.user.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}

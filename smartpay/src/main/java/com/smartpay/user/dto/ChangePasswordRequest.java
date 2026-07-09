package com.smartpay.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Password can't be blank")
    private String oldPassword;

    @NotBlank(message = "Password can't be blank")
    @Size(min = 8, message = "Minimum length of password should be 8 characters")
    private String newPassword;
}

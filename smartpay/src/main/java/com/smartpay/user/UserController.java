package com.smartpay.user;

import com.smartpay.common.response.ApiResponse;
import com.smartpay.user.dto.ChangePasswordRequest;
import com.smartpay.user.dto.UpdateProfileRequest;
import com.smartpay.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails){
        String email=userDetails.getUsername();
        UserResponse userResponse=userService.getProfile(email);
        return ResponseEntity.ok(
                ApiResponse.success("This is your data",userResponse)
        );
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile
            (@AuthenticationPrincipal UserDetails userDetails,
             @Valid
             @RequestBody UpdateProfileRequest updateProfileRequest){
        String email= userDetails.getUsername();
        UserResponse userResponse=userService.updateProfile(email,updateProfileRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated",userResponse)
        );
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                         @Valid
                                         @RequestBody  ChangePasswordRequest changePasswordRequest){
        String email= userDetails.getUsername();
        userService.changePassword(email,changePasswordRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed succsessfully",null)
        );
    }

}

package com.smartpay.admin;

import com.smartpay.admin.dto.UpdateStatusRequest;
import com.smartpay.common.response.ApiResponse;
import com.smartpay.payment.dto.PaymentResponse;
import com.smartpay.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails){
        List<UserResponse> responses=adminService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "List of users ", responses
                )
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id,
                                                                 @AuthenticationPrincipal UserDetails userDetails){
        UserResponse response=adminService.getUserById(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "The user is ",response
                )
        );
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments(@AuthenticationPrincipal UserDetails userDetails){
        List<PaymentResponse> payments=adminService.getAllPayments();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "List of payments ",payments
                )
        );
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@RequestBody UpdateStatusRequest status,
                                                                      @AuthenticationPrincipal UserDetails userDetails,
                                                                      @PathVariable UUID id){
        UserResponse response=adminService.updateUserStatus(id,status.getStatus(),userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Status updated", response
                )
        );
    }

}

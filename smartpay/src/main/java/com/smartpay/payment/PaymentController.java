package com.smartpay.payment;

import com.smartpay.common.response.ApiResponse;
import com.smartpay.payment.dto.PaymentRequest;
import com.smartpay.payment.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest paymentRequest,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        PaymentResponse paymentResponse=paymentService.processPayment(idempotencyKey,userDetails.getUsername(),paymentRequest);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment is successful",paymentResponse
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@AuthenticationPrincipal UserDetails userDetails,
    @PathVariable UUID id){
        PaymentResponse paymentResponse=paymentService.getPaymentById(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment detail of this payment id" , paymentResponse
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(@AuthenticationPrincipal UserDetails userDetails){
        List<PaymentResponse> payments=paymentService.getMyPayments(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "These are all payments for this id",payments
                )
        );
    }
}

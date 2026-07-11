package com.smartpay.wallet;

import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.common.response.ApiResponse;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.dto.CreateWalletRequest;
import com.smartpay.wallet.dto.DepositRequest;
import com.smartpay.wallet.dto.WalletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid
            @RequestBody CreateWalletRequest createWalletRequest
            ){
        UserEntity user=userRepository.findByEmail(userDetails.getUsername()).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        WalletEntity wallet = walletService.createWallet(user.getId(),createWalletRequest.getCurrency());
        WalletResponse response=new WalletResponse(wallet.getId(),wallet.getBalance(),wallet.getCurrency(),wallet.getCreatedAt());
        return  ResponseEntity.ok(
                ApiResponse.success("wallet is created with currency: "+createWalletRequest.getCurrency(),response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> getWallet(@AuthenticationPrincipal UserDetails userDetails){
        String email=userDetails.getUsername();
        List<WalletResponse> walletResponses=walletService.getWallets(email);
        return ResponseEntity.ok(
                ApiResponse.success("These wallets belongs to you",walletResponses)
        );
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody DepositRequest depositRequest){
        String email=userDetails.getUsername();
        WalletResponse wallet= walletService.deposit(id,email,depositRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Amount deopisted successfully",wallet)
        );
    }
}

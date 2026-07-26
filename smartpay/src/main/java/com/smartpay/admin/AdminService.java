package com.smartpay.admin;

import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.payment.PaymentEntity;
import com.smartpay.payment.PaymentRepository;
import com.smartpay.payment.dto.PaymentResponse;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public List<UserResponse> getAllUsers(){
        List<UserEntity> users= userRepository.findAll();
        return users.stream()
                .map(u-> new UserResponse(u.getId(),u.getEmail(),u.getFullName(),u.getRole(),u.getStatus(),u.getCreatedAt()))
                .toList();
    }

    public UserResponse getUserById(UUID id){
        UserEntity user=userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        return new UserResponse(user.getId(),user.getEmail(),user.getFullName(),user.getRole(),user.getStatus()
                ,user.getCreatedAt());
    }

    public UserResponse updateUserStatus(UUID id,String status){
        Set<String> validStatuses = Set.of("FROZEN", "ACTIVE", "SUSPENDED");

        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        UserEntity user=userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("User not found")
        );
        user.setStatus(status);
        userRepository.save(user);
        return new UserResponse(user.getId(),user.getEmail(),user.getFullName(),user.getRole(),user.getStatus()
                ,user.getCreatedAt());
    }

    public List<PaymentResponse> getAllPayments(){
        List<PaymentEntity> payments=paymentRepository.findAll();
        return payments.stream()
                .map(s-> new PaymentResponse(s.getSenderWalletId(),s.getReceiverWalletId(),s.getAmount(),s.getCurrency(),
                        s.getStatus(),s.getCreatedAt()))
                .toList();
    }
}

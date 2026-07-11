package com.smartpay.auth.service;

import com.smartpay.auth.dto.AuthResponse;
import com.smartpay.auth.dto.LoginRequest;
import com.smartpay.auth.dto.RegisterRequest;
import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.common.util.JwtUtil;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final WalletService walletService;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException(
                    "Email already registered: " + request.getEmail()
            );
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role("USER")
                .status("ACTIVE")
                .build();
        userRepository.save(user);

        walletService.createWallet(user.getId(), "INR");

        String token= jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // Step 2: Load user from DB
        UserEntity user= userRepository.findByEmail(request.getEmail()).
                orElseThrow(()-> new ResourceNotFoundException(
                        "User not found with Email: " + request.getEmail()
                ));
        // Step 3: Generate and return JWT
        String token= jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}

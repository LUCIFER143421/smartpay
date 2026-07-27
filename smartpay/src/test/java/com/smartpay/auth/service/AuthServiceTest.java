package com.smartpay.auth.service;

import com.smartpay.audit.AuditService;
import com.smartpay.auth.dto.AuthResponse;
import com.smartpay.auth.dto.LoginRequest;
import com.smartpay.auth.dto.RegisterRequest;
import com.smartpay.common.util.JwtUtil;
import com.smartpay.user.UserEntity;
import com.smartpay.user.UserRepository;
import com.smartpay.wallet.WalletEntity;
import com.smartpay.wallet.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private WalletService walletService;
    @Mock private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldReturnToken_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("priya@test.com");
        request.setPassword("secret123");
        request.setFullName("Priya");

        when(userRepository.existsByEmail("priya@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed_password");
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(i -> {
                    UserEntity saved = i.getArgument(0);
                    return UserEntity.builder()
                            .id(UUID.randomUUID())
                            .email(saved.getEmail())
                            .passwordHash(saved.getPasswordHash())
                            .fullName(saved.getFullName())
                            .role(saved.getRole())
                            .status(saved.getStatus())
                            .build();
                });
        lenient().when(walletService.createWallet(any(), anyString()))
                .thenReturn(new WalletEntity());
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("priya@test.com", response.getEmail());
        assertEquals("USER", response.getRole());
        verify(userRepository).existsByEmail("priya@test.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("secret123");
        request.setFullName("Test");

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("priya@test.com");
        request.setPassword("secret123");

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("priya@test.com")
                .role("USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("priya@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("priya@test.com", "USER")).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("priya@test.com", response.getEmail());
    }
}
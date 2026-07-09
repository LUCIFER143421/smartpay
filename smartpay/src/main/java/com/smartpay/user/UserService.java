package com.smartpay.user;

import com.smartpay.common.exception.ResourceNotFoundException;
import com.smartpay.user.dto.ChangePasswordRequest;
import com.smartpay.user.dto.UpdateProfileRequest;
import com.smartpay.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserResponse getProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: " + email));
        return new UserResponse(user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request){
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+ email));
        user.setFullName(request.getFullName());
        userRepository.save(user);
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
    public void changePassword(String email, ChangePasswordRequest request){
        UserEntity user=userRepository.findByEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+ email));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        String newPass= passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPass);
        userRepository.save(user);
    }
}

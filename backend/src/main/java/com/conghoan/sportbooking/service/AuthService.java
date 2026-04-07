package com.conghoan.sportbooking.service;

import com.conghoan.sportbooking.dto.*;
import com.conghoan.sportbooking.entity.User;
import com.conghoan.sportbooking.repository.UserRepository;
import com.conghoan.sportbooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    // Xử lí update profile
    public User updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        return userRepository.save(user);
    }
}

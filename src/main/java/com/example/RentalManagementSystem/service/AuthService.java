package com.example.RentalManagementSystem.service;

import com.example.RentalManagementSystem.dto.AuthResponse;
import com.example.RentalManagementSystem.dto.LoginRequest;
import com.example.RentalManagementSystem.dto.RegisterRequest;
import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.AccountStatus;
import com.example.RentalManagementSystem.enums.NotificationType;
import com.example.RentalManagementSystem.enums.Role;
import com.example.RentalManagementSystem.enums.VerificationStatus;
import com.example.RentalManagementSystem.repository.UserRepository;
import com.example.RentalManagementSystem.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

private final NotificationService notificationService;,

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;


    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("A user with this email already exists");
        }

        // Server-side role resolution — never trust a raw "role" field from the client.
        // Only "LANDLORD" maps to landlord; everything else (including tampering) defaults to USER.
        Role resolvedRole = "LANDLORD".equalsIgnoreCase(request.getAccountType()) ? Role.LANDLORD : Role.USER;

        String token = UUID.randomUUID().toString();

        User.UserBuilder builder = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(resolvedRole)
                .accountStatus(AccountStatus.ACTIVE);

        if (resolvedRole == Role.LANDLORD) {
            builder.emailVerified(false)
                    .verificationStatus(VerificationStatus.PENDING)
                    .verificationToken(token)
                    .verificationTokenExpiry(LocalDateTime.now().plusHours(24));
        } else {
            // USER accounts: no verification gate, can log in immediately
            builder.emailVerified(true)
                    .verificationStatus(VerificationStatus.VERIFIED);
        }

        User user = builder.build();
        userRepository.save(user);

        if (resolvedRole == Role.LANDLORD) {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
        }
        notificationService.notifyRole(
                Role.ADMIN,
                "New landlord registration",
                user.getFullName() + " (" + user.getEmail() + ") registered as a landlord and is awaiting verification.",
                "/admin/users/" + user.getId(),
                NotificationType.LANDLORD_REGISTERED
        );
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new com.example.RentalManagementSystem.exception.BadRequestException("Invalid or expired verification link"));

        if (user.getVerificationTokenExpiry() != null && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new com.example.RentalManagementSystem.exception.BadRequestException("Verification link has expired. Please register again.");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.example.RentalManagementSystem.exception.ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(toUserDetails(user));

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private org.springframework.security.core.userdetails.User toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                java.util.List.of(() -> "ROLE_" + user.getRole().name()));
    }
}
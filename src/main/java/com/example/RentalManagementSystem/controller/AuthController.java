package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.AuthResponse;
import com.example.RentalManagementSystem.dto.LoginRequest;
import com.example.RentalManagementSystem.dto.RegisterRequest;
import com.example.RentalManagementSystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Please check your email to verify your account.");
    }

    @GetMapping("/verify")
    public RedirectView verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return new RedirectView("/login?verified=true");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
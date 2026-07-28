package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.RegisterRequest;
import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.repository.UserRepository;
import com.example.RentalManagementSystem.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterViewController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @GetMapping("/register")
    public String showForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("activeTab", "register");
        return "auth";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest registerRequest,
                           HttpServletRequest request, HttpServletResponse response,
                           Model model) {
        try {
            authService.register(registerRequest);

            // auto-login right after registration
            Authentication authRequest = new UsernamePasswordAuthenticationToken(
                    registerRequest.getEmail(), registerRequest.getPassword());
            Authentication authResult = authenticationManager.authenticate(authRequest);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authResult);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);

            User user = userRepository.findByEmail(registerRequest.getEmail()).orElseThrow();
            return user.getRole().name().equals("ADMIN") ? "redirect:/admin/users" : "redirect:/";

        } catch (IllegalStateException e) {
            model.addAttribute("registerError", e.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            model.addAttribute("activeTab", "register");
            return "auth";
        }
    }
}
package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PaymentRepository;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/landlord")
@RequiredArgsConstructor
public class LandlordViewController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();

        long total = propertyRepository.findByOwnerEmail(email, PageRequest.of(0, 1000)).getTotalElements();
        long available = propertyRepository.findByOwnerEmail(email, PageRequest.of(0, 1000)).getContent()
                .stream().filter(p -> p.getStatus() == PropertyStatus.AVAILABLE).count();
        long occupied = propertyRepository.findByOwnerEmail(email, PageRequest.of(0, 1000)).getContent()
                .stream().filter(p -> p.getStatus() == PropertyStatus.RENTED).count();

        model.addAttribute("totalProperties", total);
        model.addAttribute("availableProperties", available);
        model.addAttribute("occupiedProperties", occupied);
        model.addAttribute("totalPayments", paymentRepository.count());
        model.addAttribute("recentProperties",
                propertyRepository.findByOwnerEmail(email, PageRequest.of(0, 5)).getContent());

        return "landlord/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User landlord = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("landlord", landlord);
        model.addAttribute("initials", getInitials(landlord.getFullName()));
        return "landlord/profile";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              Principal principal,
                              Model model) {
        User landlord = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!email.equalsIgnoreCase(landlord.getEmail()) && userRepository.existsByEmail(email)) {
            model.addAttribute("landlord", landlord);
            model.addAttribute("initials", getInitials(landlord.getFullName()));
            model.addAttribute("editError", "That email is already in use.");
            return "landlord/profile";
        }

        landlord.setFullName(fullName);
        landlord.setEmail(email);
        userRepository.save(landlord);

        return "redirect:/landlord/profile?updated=true";
    }

    @GetMapping("/profile/change-password")
    public String changePasswordForm(Model model, Principal principal) {
        User landlord = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("landlord", landlord);
        return "landlord/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 Model model) {
        User landlord = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("landlord", landlord);

        if (!passwordEncoder.matches(currentPassword, landlord.getPassword())) {
            model.addAttribute("pwError", "Current password is incorrect.");
            return "landlord/change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("pwError", "New password must be at least 6 characters.");
            return "landlord/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("pwError", "New password and confirmation do not match.");
            return "landlord/change-password";
        }
        if (passwordEncoder.matches(newPassword, landlord.getPassword())) {
            model.addAttribute("pwError", "New password must be different from the current password.");
            return "landlord/change-password";
        }

        landlord.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(landlord);

        return "redirect:/landlord/profile?pwChanged=true";
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
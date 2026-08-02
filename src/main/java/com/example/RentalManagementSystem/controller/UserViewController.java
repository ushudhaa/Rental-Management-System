package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.Property;
import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserViewController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Property> available = propertyRepository.findByStatus(PropertyStatus.AVAILABLE, PageRequest.of(0, 1000)).getContent();

        model.addAttribute("availableCount", available.size());
        model.addAttribute("recentProperties",
                available.stream()
                        .sorted(Comparator.comparing(Property::getCreatedAt).reversed())
                        .limit(3)
                        .collect(Collectors.toList()));

        return "user/dashboard";
    }

    @GetMapping("/properties")
    public String browse(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minRent,
            @RequestParam(required = false) BigDecimal maxRent,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        List<Property> available = propertyRepository.findByStatus(PropertyStatus.AVAILABLE, PageRequest.of(0, 1000)).getContent();

        List<Property> filtered = available.stream()
                .filter(p -> search == null || search.isBlank()
                        || p.getTitle().toLowerCase().contains(search.toLowerCase())
                        || p.getCity().toLowerCase().contains(search.toLowerCase()))
                .filter(p -> city == null || city.isBlank() || p.getCity().equalsIgnoreCase(city))
                .filter(p -> minRent == null || p.getRentAmount().compareTo(minRent) >= 0)
                .filter(p -> maxRent == null || p.getRentAmount().compareTo(maxRent) <= 0)
                .filter(p -> bedrooms == null || (p.getBedrooms() != null && p.getBedrooms().equals(bedrooms)))
                .collect(Collectors.toList());

        switch (sort) {
            case "rent_low" -> filtered.sort(Comparator.comparing(Property::getRentAmount));
            case "rent_high" -> filtered.sort(Comparator.comparing(Property::getRentAmount).reversed());
            case "name_asc" -> filtered.sort(Comparator.comparing(Property::getTitle, String.CASE_INSENSITIVE_ORDER));
            default -> filtered.sort(Comparator.comparing(Property::getCreatedAt).reversed());
        }

        List<String> cities = available.stream()
                .map(Property::getCity)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("properties", filtered);
        model.addAttribute("cities", cities);
        model.addAttribute("search", search);
        model.addAttribute("city", city);
        model.addAttribute("minRent", minRent);
        model.addAttribute("maxRent", maxRent);
        model.addAttribute("bedrooms", bedrooms);
        model.addAttribute("sort", sort);

        return "user/properties";
    }

    @GetMapping("/properties/{id}")
    public String propertyDetail(@PathVariable Long id, Model model) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new ResourceNotFoundException("Property not found");
        }

        model.addAttribute("property", property);
        return "user/property-detail";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("appUser", user);
        model.addAttribute("initials", getInitials(user.getFullName()));
        return "user/profile";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              Principal principal,
                              Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            model.addAttribute("appUser", user);
            model.addAttribute("initials", getInitials(user.getFullName()));
            model.addAttribute("editError", "That email is already in use.");
            return "user/profile";
        }

        user.setFullName(fullName);
        user.setEmail(email);
        userRepository.save(user);

        return "redirect:/user/profile?updated=true";
    }

    @GetMapping("/profile/change-password")
    public String changePasswordForm(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("appUser", user);
        return "user/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("appUser", user);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            model.addAttribute("pwError", "Current password is incorrect.");
            return "user/change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("pwError", "New password must be at least 6 characters.");
            return "user/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("pwError", "New password and confirmation do not match.");
            return "user/change-password";
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            model.addAttribute("pwError", "New password must be different from the current password.");
            return "user/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/user/profile?pwChanged=true";
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
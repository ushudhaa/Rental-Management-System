package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.enums.Role;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalLandlords", userRepository.countByRole(Role.LANDLORD));
        model.addAttribute("verifiedLandlords", userRepository.countByRoleAndEmailVerifiedTrue(Role.LANDLORD));
        model.addAttribute("pendingLandlords", userRepository.countByRoleAndEmailVerifiedFalse(Role.LANDLORD));
        model.addAttribute("totalProperties", propertyRepository.count());
        model.addAttribute("availableProperties", propertyRepository.countByStatus(PropertyStatus.AVAILABLE));
        model.addAttribute("recentUsers", userRepository.findTop5ByOrderByCreatedAtDesc());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        List<User> allUsers = userRepository.findAll();

        List<User> filtered = allUsers.stream()
                .filter(u -> search == null || search.isBlank()
                        || u.getFullName().toLowerCase().contains(search.toLowerCase())
                        || u.getEmail().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> role == null || role.isBlank() || u.getRole().name().equals(role))
                .filter(u -> status == null || status.isBlank()
                        || (status.equals("VERIFIED") && u.isEmailVerified())
                        || (status.equals("PENDING") && !u.isEmailVerified()))
                .collect(java.util.stream.Collectors.toList());

        switch (sort) {
            case "oldest" -> filtered.sort(Comparator.comparing(User::getCreatedAt));
            case "name_asc" -> filtered.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
            case "name_desc" -> filtered.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER).reversed());
            default -> filtered.sort(Comparator.comparing(User::getCreatedAt).reversed());
        }

        model.addAttribute("users", filtered);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalAdmins", allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count());
        model.addAttribute("totalLandlords", allUsers.stream().filter(u -> u.getRole() == Role.LANDLORD).count());
        model.addAttribute("totalPending", allUsers.stream().filter(u -> !u.isEmailVerified()).count());

        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);

        return "admin/users";
    }

    @GetMapping("/landlords/{id}")
    public String landlordDetail(@PathVariable Long id, Model model) {
        User landlord = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("landlord", landlord);
        model.addAttribute("properties",
                propertyRepository.findByOwnerEmail(landlord.getEmail(), PageRequest.of(0, 50)).getContent());
        return "admin/landlord-detail";
    }
}
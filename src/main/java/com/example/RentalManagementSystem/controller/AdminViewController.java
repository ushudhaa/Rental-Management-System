package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.AccountStatus;
import com.example.RentalManagementSystem.enums.NotificationType;
import com.example.RentalManagementSystem.enums.Role;
import com.example.RentalManagementSystem.enums.VerificationStatus;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.UserRepository;
import com.example.RentalManagementSystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalLandlords", userRepository.countByRole(Role.LANDLORD));
        model.addAttribute("verifiedLandlords", userRepository.countByRoleAndEmailVerifiedTrue(Role.LANDLORD));
        model.addAttribute("pendingLandlords", userRepository.countByRoleAndEmailVerifiedFalse(Role.LANDLORD));
        model.addAttribute("recentUsers", userRepository.findTop5ByOrderByCreatedAtDesc());
        model.addAttribute("recentActivity", buildRecentActivity());
        model.addAttribute("pendingCount", userRepository.countByRoleAndVerificationStatus(Role.LANDLORD, VerificationStatus.PENDING));
        return "admin/dashboard";
    }

    private List<ActivityItem> buildRecentActivity() {
        List<ActivityItem> activity = new java.util.ArrayList<>();

        for (User u : userRepository.findTop5ByOrderByCreatedAtDesc()) {
            activity.add(new ActivityItem(
                    u.getFullName() + " registered as a " + u.getRole().name().toLowerCase() + ".",
                    u.getCreatedAt()));
        }

        return activity.stream()
                .sorted(Comparator.comparing(ActivityItem::timestamp).reversed())
                .limit(8)
                .collect(Collectors.toList());
    }

    public record ActivityItem(String description, java.time.LocalDateTime timestamp) {}

    @GetMapping("/users")
    public String users(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<User> allUsers = userRepository.findAll();

        List<User> filtered = allUsers.stream()
                .filter(u -> search == null || search.isBlank()
                        || u.getFullName().toLowerCase().contains(search.toLowerCase())
                        || u.getEmail().toLowerCase().contains(search.toLowerCase()))
                .filter(u -> role == null || role.isBlank() || u.getRole().name().equals(role))
                .filter(u -> status == null || status.isBlank()
                        || u.getVerificationStatus().name().equals(status))
                .collect(Collectors.toList());

        switch (sort) {
            case "oldest" -> filtered.sort(Comparator.comparing(User::getCreatedAt));
            case "name_asc" -> filtered.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
            case "name_desc" -> filtered.sort(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER).reversed());
            default -> filtered.sort(Comparator.comparing(User::getCreatedAt).reversed());
        }

        int totalFiltered = filtered.size();
        int totalPages = (int) Math.ceil((double) totalFiltered / size);
        int fromIndex = Math.min(page * size, totalFiltered);
        int toIndex = Math.min(fromIndex + size, totalFiltered);
        List<User> pageContent = filtered.subList(fromIndex, toIndex);

        model.addAttribute("users", pageContent);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalAdmins", allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count());
        model.addAttribute("totalLandlords", allUsers.stream().filter(u -> u.getRole() == Role.LANDLORD).count());
        model.addAttribute("totalPending", allUsers.stream().filter(u -> u.getVerificationStatus() == VerificationStatus.PENDING).count());

        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalFiltered", totalFiltered);
        model.addAttribute("showingFrom", totalFiltered == 0 ? 0 : fromIndex + 1);
        model.addAttribute("showingTo", toIndex);

        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    @PostMapping("/users/{id}/verify")
    public String verify(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setRejectionReason(null);
        userRepository.save(user);
        notificationService.notifyUser(user, "Account verified",
                "Your landlord account has been verified. You can now list properties.",
                "/landlord/dashboard", NotificationType.LANDLORD_VERIFIED);
        return "redirect:/admin/users/" + id + "?verified=true";
    }

    @PostMapping("/users/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerificationStatus(VerificationStatus.REJECTED);
        user.setRejectionReason(reason);
        userRepository.save(user);
        notificationService.notifyUser(user, "Account verification rejected",
                (reason != null && !reason.isBlank())
                        ? "Your landlord verification was rejected. Reason: " + reason
                        : "Your landlord verification was rejected.",
                "/landlord/profile", NotificationType.LANDLORD_REJECTED);
        return "redirect:/admin/users/" + id + "?rejected=true";
    }

    @PostMapping("/users/{id}/disable")
    public String disable(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.DISABLED);
        userRepository.save(user);
        return "redirect:/admin/users/" + id + "?disabled=true";
    }

    @PostMapping("/users/{id}/activate")
    public String activate(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        return "redirect:/admin/users/" + id + "?activated=true";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("admin", admin);
        model.addAttribute("initials", getInitials(admin.getFullName()));
        return "admin/profile";
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam String fullName,
                              @RequestParam String email,
                              Principal principal,
                              Model model) {
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!email.equalsIgnoreCase(admin.getEmail()) && userRepository.existsByEmail(email)) {
            model.addAttribute("admin", admin);
            model.addAttribute("editError", "That email is already in use.");
            return "admin/profile";
        }

        admin.setFullName(fullName);
        admin.setEmail(email);
        userRepository.save(admin);

        return "redirect:/admin/profile?updated=true";
    }

    @GetMapping("/profile/change-password")
    public String changePasswordForm(Model model, Principal principal) {
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        model.addAttribute("admin", admin);
        return "admin/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 Model model) {
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("admin", admin);

        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            model.addAttribute("pwError", "Current password is incorrect.");
            return "admin/change-password";
        }
        if (newPassword.length() < 6) {
            model.addAttribute("pwError", "New password must be at least 6 characters.");
            return "admin/change-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("pwError", "New password and confirmation do not match.");
            return "admin/change-password";
        }
        if (passwordEncoder.matches(newPassword, admin.getPassword())) {
            model.addAttribute("pwError", "New password must be different from the current password.");
            return "admin/change-password";
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(admin);

        return "redirect:/admin/profile?pwChanged=true";
    }
}
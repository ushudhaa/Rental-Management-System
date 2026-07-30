package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.Property;
import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.AccountStatus;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.enums.Role;
import com.example.RentalManagementSystem.enums.VerificationStatus;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        for (Property p : propertyRepository.findAll()) {
            if (p.getCreatedAt() != null) {
                activity.add(new ActivityItem(
                        "A new property \"" + p.getTitle() + "\" was added.",
                        p.getCreatedAt()));
            }
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
        if (user.getRole() == Role.LANDLORD) {
            model.addAttribute("properties",
                    propertyRepository.findByOwnerEmail(user.getEmail(), PageRequest.of(0, 50)).getContent());
        }
        return "admin/user-detail";
    }

    @PostMapping("/users/{id}/verify")
    public String verify(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setRejectionReason(null);
        userRepository.save(user);
        return "redirect:/admin/users/" + id + "?verified=true";
    }

    @PostMapping("/users/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setVerificationStatus(VerificationStatus.REJECTED);
        user.setRejectionReason(reason);
        userRepository.save(user);
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

    @GetMapping("/landlords/{id}")
    public String landlordDetail(@PathVariable Long id) {
        return "redirect:/admin/users/" + id;
    }

    @GetMapping("/properties")
    public String properties(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {

        List<Property> allProperties = propertyRepository.findAll();

        List<Property> filtered = allProperties.stream()
                .filter(p -> search == null || search.isBlank()
                        || p.getTitle().toLowerCase().contains(search.toLowerCase())
                        || p.getCity().toLowerCase().contains(search.toLowerCase())
                        || (p.getOwner() != null && p.getOwner().getFullName().toLowerCase().contains(search.toLowerCase())))
                .filter(p -> status == null || status.isBlank() || p.getStatus().name().equals(status))
                .sorted(Comparator.comparing(Property::getCreatedAt).reversed())
                .collect(Collectors.toList());

        model.addAttribute("properties", filtered);
        model.addAttribute("totalProperties", allProperties.size());
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "admin/properties";
    }

    @PostMapping("/properties/{id}/disable")
    public String disableProperty(@PathVariable Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setStatus(PropertyStatus.INACTIVE);
        propertyRepository.save(property);
        return "redirect:/admin/properties";
    }

    @PostMapping("/properties/{id}/activate")
    public String activateProperty(@PathVariable Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setStatus(PropertyStatus.AVAILABLE);
        propertyRepository.save(property);
        return "redirect:/admin/properties";
    }

    @PostMapping("/properties/{id}/delete")
    public String deleteProperty(@PathVariable Long id) {
        propertyRepository.deleteById(id);
        return "redirect:/admin/properties";
    }
}
package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PaymentRepository;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/landlords/{id}")
    public String landlordDetail(@PathVariable Long id, Model model) {
        User landlord = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        model.addAttribute("landlord", landlord);
        model.addAttribute("properties", propertyRepository.findByOwnerEmail(landlord.getEmail(), PageRequest.of(0, 50)).getContent());
        return "admin/landlord-detail";
    }
}
package com.example.RentalManagementSystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {

            Set<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            if (authorities.contains("ROLE_ADMIN")) return "redirect:/admin/dashboard";
            if (authorities.contains("ROLE_LANDLORD")) return "redirect:/landlord/dashboard";
            return "redirect:/user/dashboard";
        }
        return "index";
    }
}
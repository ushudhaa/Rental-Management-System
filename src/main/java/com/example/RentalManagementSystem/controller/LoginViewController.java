package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("activeTab", "login");
        return "auth";
    }
}
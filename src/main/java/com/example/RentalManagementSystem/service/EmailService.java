package com.example.RentalManagementSystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String link = baseUrl + "/api/v1/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your Rental Management System account");
        message.setText(
                "Hi " + fullName + ",\n\n" +
                        "Please confirm your email address by clicking the link below:\n" +
                        link + "\n\n" +
                        "This link expires in 24 hours.\n\n" +
                        "If you didn't request this, you can ignore this email."
        );
        mailSender.send(message);
    }
}
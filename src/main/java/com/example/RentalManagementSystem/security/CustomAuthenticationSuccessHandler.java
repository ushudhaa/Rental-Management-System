package com.example.RentalManagementSystem.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        String redirectUrl;
        if (authorities.contains("ROLE_ADMIN")) {
            redirectUrl = "/admin/dashboard";
        } else if (authorities.contains("ROLE_LANDLORD")) {
            redirectUrl = "/landlord/dashboard";
        } else {
            redirectUrl = "/user/dashboard";
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
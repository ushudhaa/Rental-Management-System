package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.ProfileResponse;
import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.exception.BadRequestException;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> uploadPhoto(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (file.isEmpty()) {
            throw new BadRequestException("No file was selected.");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, and WEBP images are allowed.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("Image must be smaller than 5 MB.");
        }

        Path dirPath = Path.of(uploadDir);
        Files.createDirectories(dirPath);

        String extension = switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String filename = UUID.randomUUID() + extension;
        Path targetPath = dirPath.resolve(filename);
        file.transferTo(targetPath);

        // remove old file if it exists
        if (user.getProfileImageUrl() != null) {
            String oldFilename = user.getProfileImageUrl().substring(user.getProfileImageUrl().lastIndexOf('/') + 1);
            File oldFile = new File(uploadDir, oldFilename);
            if (oldFile.exists()) oldFile.delete();
        }

        user.setProfileImageUrl("/uploads/profile-images/" + filename);
        userRepository.save(user);

        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<ProfileResponse> deletePhoto(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getProfileImageUrl() != null) {
            String oldFilename = user.getProfileImageUrl().substring(user.getProfileImageUrl().lastIndexOf('/') + 1);
            File oldFile = new File(uploadDir, oldFilename);
            if (oldFile.exists()) oldFile.delete();
            user.setProfileImageUrl(null);
            userRepository.save(user);
        }

        return ResponseEntity.ok(toResponse(user));
    }

    private ProfileResponse toResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accountStatus(user.getAccountStatus().name())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
package com.example.RentalManagementSystem.repository;

import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String token);

    long countByRole(Role role);
    long countByRoleAndEmailVerifiedTrue(Role role);
    long countByRoleAndEmailVerifiedFalse(Role role);

    List<User> findTop5ByOrderByCreatedAtDesc();
}
package com.example.RentalManagementSystem.repository;

import com.example.RentalManagementSystem.entity.Property;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);
    Page<Property> findByCityIgnoreCaseContaining(String city, Pageable pageable);
    Page<Property> findByOwnerEmail(String email, Pageable pageable);
    long countByStatus(PropertyStatus status);
}
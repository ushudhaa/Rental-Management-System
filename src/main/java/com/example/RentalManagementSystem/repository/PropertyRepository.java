package com.example.RentalManagementSystem.repository;

import com.example.RentalManagementSystem.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
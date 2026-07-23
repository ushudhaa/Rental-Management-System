package com.example.RentalManagementSystem.repository;

import com.example.RentalManagementSystem.entity.Payment;
import com.example.RentalManagementSystem.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByPropertyId(Long propertyId, Pageable pageable);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
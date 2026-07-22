package com.example.RentalManagementSystem.repository;

import com.example.RentalManagementSystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByAmount(Integer amount);

}
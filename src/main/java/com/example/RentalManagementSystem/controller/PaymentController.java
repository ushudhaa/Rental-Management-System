package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.entity.Payment;
import com.example.RentalManagementSystem.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create
    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // Read By Id
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable String id,
            @RequestBody Payment payment) {

        return ResponseEntity.ok(paymentService.updatePayment(id, payment));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable String id) {

        paymentService.deletePayment(id);
        return ResponseEntity.ok("Payment deleted successfully.");
    }
}
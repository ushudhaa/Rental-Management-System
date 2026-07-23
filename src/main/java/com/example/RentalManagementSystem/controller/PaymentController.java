package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.PaymentRequest;
import com.example.RentalManagementSystem.dto.PaymentResponse;
import com.example.RentalManagementSystem.enums.PaymentStatus;
import com.example.RentalManagementSystem.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAll(
            @RequestParam(required = false) Long propertyId,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {

        if (propertyId != null) {
            return ResponseEntity.ok(paymentService.getByProperty(propertyId, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(paymentService.getByStatus(status, pageable));
        }
        return ResponseEntity.ok(paymentService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.update(id, request));
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponse> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.markAsPaid(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
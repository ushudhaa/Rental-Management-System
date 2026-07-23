package com.example.RentalManagementSystem.service;

import com.example.RentalManagementSystem.dto.PaymentRequest;
import com.example.RentalManagementSystem.dto.PaymentResponse;
import com.example.RentalManagementSystem.entity.Payment;
import com.example.RentalManagementSystem.entity.Property;
import com.example.RentalManagementSystem.enums.PaymentStatus;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PaymentRepository;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));

        Payment payment = Payment.builder()
                .property(property)
                .tenantName(request.getTenantName())
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .status(request.getStatus() != null ? request.getStatus() : PaymentStatus.PENDING)
                .referenceNumber(request.getReferenceNumber())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public Page<PaymentResponse> getAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<PaymentResponse> getByProperty(Long propertyId, Pageable pageable) {
        return paymentRepository.findByPropertyId(propertyId, pageable).map(this::toResponse);
    }

    public Page<PaymentResponse> getByStatus(PaymentStatus status, Pageable pageable) {
        return paymentRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional
    public PaymentResponse update(Long id, PaymentRequest request) {
        Payment payment = findEntity(id);
        if (!request.getPropertyId().equals(payment.getProperty().getId())) {
            Property property = propertyRepository.findById(request.getPropertyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + request.getPropertyId()));
            payment.setProperty(property);
        }
        payment.setTenantName(request.getTenantName());
        payment.setAmount(request.getAmount());
        payment.setDueDate(request.getDueDate());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setReferenceNumber(request.getReferenceNumber());
        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
        }
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse markAsPaid(Long id) {
        Payment payment = findEntity(id);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDate.now());
        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public void delete(Long id) {
        paymentRepository.delete(findEntity(id));
    }

    private Payment findEntity(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .propertyId(p.getProperty().getId())
                .propertyTitle(p.getProperty().getTitle())
                .tenantName(p.getTenantName())
                .amount(p.getAmount())
                .dueDate(p.getDueDate())
                .paymentDate(p.getPaymentDate())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .referenceNumber(p.getReferenceNumber())
                .build();
    }
}
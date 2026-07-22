package com.example.RentalManagementSystem.service;

import com.example.RentalManagementSystem.entity.Payment;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Create
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    // Read All
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Read By Id
    public Payment getPaymentById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found with id : " + id));
    }

    // Update
    public Payment updatePayment(String id, Payment payment) {

        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found with id : " + id));

        existingPayment.setAmount(payment.getAmount());
        existingPayment.setPaymentDate(payment.getPaymentDate());
        existingPayment.setPaymentMethod(payment.getPaymentMethod());
        existingPayment.setProperty(payment.getProperty());

        return paymentRepository.save(existingPayment);
    }

    // Delete
    public void deletePayment(String id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found with id : " + id));

        paymentRepository.delete(payment);
    }
}
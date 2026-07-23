package com.example.RentalManagementSystem.dto;

import com.example.RentalManagementSystem.enums.PaymentMethod;
import com.example.RentalManagementSystem.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Property id is required")
    private Long propertyId;

    @NotBlank(message = "Tenant name is required")
    private String tenantName;

    @NotNull(message = "Amount is required")
    @Positive
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String referenceNumber;
}
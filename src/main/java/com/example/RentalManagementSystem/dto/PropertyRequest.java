package com.example.RentalManagementSystem.dto;

import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PropertyRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String zipCode;

    @NotNull(message = "Property type is required")
    private PropertyType type;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be positive")
    private BigDecimal rentAmount;

    private Integer bedrooms;
    private Integer bathrooms;
    private Double areaSqft;
    private PropertyStatus status;
}
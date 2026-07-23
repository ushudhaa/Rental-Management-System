package com.example.RentalManagementSystem.dto;

import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private PropertyType type;
    private BigDecimal rentAmount;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double areaSqft;
    private PropertyStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
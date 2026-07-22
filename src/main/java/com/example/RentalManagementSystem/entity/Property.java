package com.example.RentalManagementSystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyName;

    private String address;

    private String propertyType;

    private BigDecimal monthlyRent;

    private Boolean deleted;

    @Enumerated(EnumType.STRING)
    private PropertyStatus status;

    public enum PropertyStatus {
        AVAILABLE,
        OCCUPIED,
        UNDER_MAINTENANCE
    }
}
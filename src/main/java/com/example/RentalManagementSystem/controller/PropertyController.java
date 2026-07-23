package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.PropertyRequest;
import com.example.RentalManagementSystem.dto.PropertyResponse;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PropertyResponse>> getAll(
            @RequestParam(required = false) PropertyStatus status,
            @RequestParam(required = false) String city,
            Pageable pageable) {

        if (status != null) {
            return ResponseEntity.ok(propertyService.getByStatus(status, pageable));
        }
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(propertyService.searchByCity(city, pageable));
        }
        return ResponseEntity.ok(propertyService.getAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        propertyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
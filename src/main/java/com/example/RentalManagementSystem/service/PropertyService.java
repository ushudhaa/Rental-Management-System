package com.example.RentalManagementSystem.service;

import com.example.RentalManagementSystem.dto.PropertyRequest;
import com.example.RentalManagementSystem.dto.PropertyResponse;
import com.example.RentalManagementSystem.entity.Property;
import com.example.RentalManagementSystem.enums.PropertyStatus;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .type(request.getType())
                .rentAmount(request.getRentAmount())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .areaSqft(request.getAreaSqft())
                .status(request.getStatus() != null ? request.getStatus() : PropertyStatus.AVAILABLE)
                .build();

        return toResponse(propertyRepository.save(property));
    }

    public PropertyResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public Page<PropertyResponse> getAll(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<PropertyResponse> getByStatus(PropertyStatus status, Pageable pageable) {
        return propertyRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    public Page<PropertyResponse> searchByCity(String city, Pageable pageable) {
        return propertyRepository.findByCityIgnoreCaseContaining(city, pageable).map(this::toResponse);
    }

    @Transactional
    public PropertyResponse update(Long id, PropertyRequest request) {
        Property property = findEntity(id);
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setZipCode(request.getZipCode());
        property.setType(request.getType());
        property.setRentAmount(request.getRentAmount());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setAreaSqft(request.getAreaSqft());
        if (request.getStatus() != null) {
            property.setStatus(request.getStatus());
        }
        return toResponse(propertyRepository.save(property));
    }

    @Transactional
    public void delete(Long id) {
        propertyRepository.delete(findEntity(id));
    }

    private Property findEntity(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
    }

    private PropertyResponse toResponse(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .address(p.getAddress())
                .city(p.getCity())
                .state(p.getState())
                .zipCode(p.getZipCode())
                .type(p.getType())
                .rentAmount(p.getRentAmount())
                .bedrooms(p.getBedrooms())
                .bathrooms(p.getBathrooms())
                .areaSqft(p.getAreaSqft())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
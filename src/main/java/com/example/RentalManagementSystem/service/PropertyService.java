package com.example.RentalManagementSystem.service;

import com.example.RentalManagementSystem.entity.Property;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    // Create
    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    // Read All
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    // Read By Id
    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found with id: " + id));
    }

    // Update
    public Property updateProperty(Long id, Property property) {

        Property existingProperty = propertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found with id: " + id));

        existingProperty.setPropertyName(property.getPropertyName());
        existingProperty.setAddress(property.getAddress());
        existingProperty.setRentAmount(property.getRentAmount());
        existingProperty.setOwnerName(property.getOwnerName());
        existingProperty.setStatus(property.getStatus());

        return propertyRepository.save(existingProperty);
    }

    // Delete
    public void deleteProperty(Long id) {

        Property property = propertyRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found with id: " + id));

        propertyRepository.delete(property);
    }
}
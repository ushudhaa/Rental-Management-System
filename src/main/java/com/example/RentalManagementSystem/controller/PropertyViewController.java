package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.PropertyRequest;
import com.example.RentalManagementSystem.dto.PropertyResponse;
import com.example.RentalManagementSystem.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/landlord/properties")
@RequiredArgsConstructor
public class PropertyViewController {

    private final PropertyService propertyService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model, Authentication authentication) {
        model.addAttribute("propertyPage", propertyService.getByOwner(authentication.getName(), PageRequest.of(page, 8)));
        return "landlord/properties/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("propertyRequest", new PropertyRequest());
        model.addAttribute("isEdit", false);
        return "landlord/properties/form";
    }

    @PostMapping
    public String create(@ModelAttribute PropertyRequest propertyRequest, Authentication authentication) {
        propertyService.create(propertyRequest, authentication.getName());
        return "redirect:/landlord/properties";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("property", propertyService.getById(id));
        return "landlord/properties/detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        PropertyResponse existing = propertyService.getById(id);

        PropertyRequest form = new PropertyRequest();
        form.setTitle(existing.getTitle());
        form.setDescription(existing.getDescription());
        form.setAddress(existing.getAddress());
        form.setCity(existing.getCity());
        form.setState(existing.getState());
        form.setZipCode(existing.getZipCode());
        form.setType(existing.getType());
        form.setRentAmount(existing.getRentAmount());
        form.setBedrooms(existing.getBedrooms());
        form.setBathrooms(existing.getBathrooms());
        form.setAreaSqft(existing.getAreaSqft());
        form.setStatus(existing.getStatus());

        model.addAttribute("propertyRequest", form);
        model.addAttribute("propertyId", id);
        model.addAttribute("isEdit", true);
        return "landlord/properties/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute PropertyRequest propertyRequest) {
        propertyService.update(id, propertyRequest);
        return "redirect:/landlord/properties";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        propertyService.delete(id);
        return "redirect:/landlord/properties";
    }
}
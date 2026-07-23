package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.PaymentRequest;
import com.example.RentalManagementSystem.dto.PaymentResponse;
import com.example.RentalManagementSystem.service.PaymentService;
import com.example.RentalManagementSystem.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;
    private final PropertyService propertyService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("paymentPage", paymentService.getAll(PageRequest.of(page, 8)));
        return "payments/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("paymentRequest", new PaymentRequest());
        model.addAttribute("properties", propertyService.getAll(PageRequest.of(0, 100)).getContent());
        model.addAttribute("isEdit", false);
        return "payments/form";
    }

    @PostMapping
    public String create(@ModelAttribute PaymentRequest paymentRequest) {
        paymentService.create(paymentRequest);
        return "redirect:/payments";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        PaymentResponse existing = paymentService.getById(id);

        PaymentRequest form = new PaymentRequest();
        form.setPropertyId(existing.getPropertyId());
        form.setTenantName(existing.getTenantName());
        form.setAmount(existing.getAmount());
        form.setDueDate(existing.getDueDate());
        form.setPaymentDate(existing.getPaymentDate());
        form.setPaymentMethod(existing.getPaymentMethod());
        form.setStatus(existing.getStatus());
        form.setReferenceNumber(existing.getReferenceNumber());

        model.addAttribute("paymentRequest", form);
        model.addAttribute("paymentId", id);
        model.addAttribute("properties", propertyService.getAll(PageRequest.of(0, 100)).getContent());
        model.addAttribute("isEdit", true);
        return "payments/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute PaymentRequest paymentRequest) {
        paymentService.update(id, paymentRequest);
        return "redirect:/payments";
    }

    @PostMapping("/{id}/mark-paid")
    public String markPaid(@PathVariable Long id) {
        paymentService.markAsPaid(id);
        return "redirect:/payments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        paymentService.delete(id);
        return "redirect:/payments";
    }
}
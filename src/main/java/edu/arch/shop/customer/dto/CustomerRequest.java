package edu.arch.shop.customer.dto;

import jakarta.persistence.Column;

import java.math.BigDecimal;

public record CustomerRequest(
        String name,
        String surname,
        Integer numberOfPurchases
) {
}
package com.example.transactionstarter.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Currency {

    INR,
    USD,
    GBP,
    EUR,
    CAD,
    AUD;

    @JsonCreator
    public static Currency fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (Currency currency : Currency.values()) {
            if (currency.name().equals(normalized)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Invalid currency: '" + value + "'. Accepted values: [INR, USD, GBP, EUR, CAD, AUD]");
    }
}
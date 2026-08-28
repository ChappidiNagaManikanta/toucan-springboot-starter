package com.example.transactionstarter.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType {

    PAYMENT,
    REFUND,
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL;

    @JsonCreator
    public static TransactionType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type: '" + value + "'. Accepted values: [PAYMENT, REFUND, TRANSFER, DEPOSIT, WITHDRAWAL]");
    }
}
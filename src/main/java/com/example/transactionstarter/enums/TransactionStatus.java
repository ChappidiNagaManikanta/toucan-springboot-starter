package com.example.transactionstarter.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionStatus {

    PENDING,
    COMPLETED,
    APPROVED,
    SUCCESS,
    FAILED,
    REJECTED,
    CANCELLED;

    @JsonCreator
    public static TransactionStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        for (TransactionStatus status : TransactionStatus.values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        if ("SUCCESSFUL".equals(normalized)) {
            return COMPLETED;
        }
        if ("DECLINED".equals(normalized)) {
            return FAILED;
        }
        throw new IllegalArgumentException("Invalid status: '" + value + "'. Accepted values: [PENDING, COMPLETED, APPROVED, SUCCESS, FAILED, REJECTED, CANCELLED]");
    }
}
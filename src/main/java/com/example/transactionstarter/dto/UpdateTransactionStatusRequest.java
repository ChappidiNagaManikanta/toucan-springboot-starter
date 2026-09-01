package com.example.transactionstarter.dto;

import com.example.transactionstarter.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;

public class UpdateTransactionStatusRequest {

    @NotNull
    @JsonAlias({"Transaction Status", "transaction_status", "TransactionStatus", "status"})
    private TransactionStatus transactionStatus;

    public UpdateTransactionStatusRequest() {
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
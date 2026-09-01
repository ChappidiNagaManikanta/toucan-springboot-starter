package com.example.transactionstarter.dto;

import java.math.BigDecimal;

import com.example.transactionstarter.enums.Currency;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateTransactionRequest {

    @NotBlank
    @JsonAlias({"Transaction ID", "transaction_id", "TransactionId"})
    private String transactionId;

    @NotBlank
    @JsonAlias({"Customer ID", "customer_id", "CustomerId"})
    private String customerId;

    @NotNull
    @Positive
    @JsonAlias({"Amount"})
    private BigDecimal amount;

    @NotNull
    @JsonAlias({"Currency"})
    private Currency currency;

    @NotNull
    @JsonAlias({"Transaction Type", "transaction_type", "TransactionType"})
    private TransactionType transactionType;

    @JsonAlias({"Transaction Status", "transaction_status", "TransactionStatus", "status"})
    private TransactionStatus transactionStatus;

    public CreateTransactionRequest() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
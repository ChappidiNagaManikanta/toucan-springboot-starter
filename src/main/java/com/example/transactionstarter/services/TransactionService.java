package com.example.transactionstarter.services;

import java.util.List;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.model.Transaction;

public interface TransactionService {

    Transaction createTransaction(CreateTransactionRequest request);

    Transaction getTransactionById(String transactionId);

    Transaction updateTransactionStatus(
            String transactionId,
            TransactionStatus status);

    List<Transaction> getTransactionsByCustomerId(String customerId);
    
}
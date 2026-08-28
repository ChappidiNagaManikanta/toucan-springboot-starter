package com.example.transactionstarter.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.exceptions.DuplicateTransactionException;
import com.example.transactionstarter.exceptions.TransactionNotFoundException;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    // Constructor injection
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // 1. Create Transaction
    @Override
    public Transaction createTransaction(CreateTransactionRequest request) {

        // Check whether transaction ID already exists
        if (transactionRepository.existsById(request.getTransactionId())) {
            throw new DuplicateTransactionException(
                    "Transaction already exists with ID: "
                    + request.getTransactionId());
        }

        // Create a new Transaction object
        Transaction transaction = new Transaction();

        transaction.setTransactionId(request.getTransactionId());
        transaction.setCustomerId(request.getCustomerId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setTransactionType(request.getTransactionType());

        // Default initial status to PENDING if null
        TransactionStatus initialStatus = request.getTransactionStatus();
        if (initialStatus == null) {
            initialStatus = TransactionStatus.PENDING;
        }
        transaction.setTransactionStatus(initialStatus);

        // Save transaction into database
        return transactionRepository.save(transaction);
    }


    // 2. Get Transaction by ID
    @Override
    public Transaction getTransactionById(String transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found with ID: "
                                + transactionId));
    }


    // 3. Update Transaction Status
    @Override
    public Transaction updateTransactionStatus(
            String transactionId,
            TransactionStatus status) {

        if (status == null) {
            throw new IllegalArgumentException("New transaction status cannot be null");
        }

        // Find the transaction
        Transaction transaction =
                transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found with ID: "
                                + transactionId));

        // Get current status
        TransactionStatus currentStatus =
                transaction.getTransactionStatus();

        // Same status check
        if (currentStatus == status) {
            throw new IllegalArgumentException(
                    "Transaction is already in status: " + currentStatus);
        }

        // Terminal statuses (COMPLETED, SUCCESS, FAILED, REJECTED, CANCELLED) cannot be changed
        if (isTerminalStatus(currentStatus)) {
            throw new IllegalArgumentException(
                    "Transaction status cannot be changed from terminal status: "
                    + currentStatus);
        }

        // Validate allowed status transitions
        if (!isValidTransition(currentStatus, status)) {
            throw new IllegalArgumentException(
                    "Cannot transition transaction status from "
                    + currentStatus + " to " + status);
        }

        // Update status
        transaction.setTransactionStatus(status);

        // Save updated transaction
        return transactionRepository.save(transaction);
    }

    private boolean isTerminalStatus(TransactionStatus status) {
        return status == TransactionStatus.COMPLETED
                || status == TransactionStatus.SUCCESS
                || status == TransactionStatus.FAILED
                || status == TransactionStatus.REJECTED
                || status == TransactionStatus.CANCELLED;
    }

    private boolean isValidTransition(TransactionStatus currentStatus, TransactionStatus newStatus) {
        if (currentStatus == TransactionStatus.PENDING) {
            // PENDING can transition to any valid subsequent state
            return newStatus == TransactionStatus.APPROVED
                    || newStatus == TransactionStatus.COMPLETED
                    || newStatus == TransactionStatus.SUCCESS
                    || newStatus == TransactionStatus.FAILED
                    || newStatus == TransactionStatus.REJECTED
                    || newStatus == TransactionStatus.CANCELLED;
        } else if (currentStatus == TransactionStatus.APPROVED) {
            // APPROVED can transition to COMPLETED, SUCCESS, FAILED, or CANCELLED
            return newStatus == TransactionStatus.COMPLETED
                    || newStatus == TransactionStatus.SUCCESS
                    || newStatus == TransactionStatus.FAILED
                    || newStatus == TransactionStatus.CANCELLED;
        }
        return false;
    }


    // 4. Get all transactions for a customer
    @Override
    public List<Transaction> getTransactionsByCustomerId(
            String customerId) {

        return transactionRepository.findByCustomerId(customerId);
    }
}
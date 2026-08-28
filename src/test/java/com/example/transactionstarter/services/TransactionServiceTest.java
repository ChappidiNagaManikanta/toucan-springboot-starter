package com.example.transactionstarter.services;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.enums.Currency;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.enums.TransactionType;
import com.example.transactionstarter.exceptions.DuplicateTransactionException;
import com.example.transactionstarter.exceptions.TransactionNotFoundException;
import com.example.transactionstarter.model.Transaction;

@SpringBootTest
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Test
    void testCreateTransaction_Success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN_TEST_1");
        request.setCustomerId("CUST_100");
        request.setAmount(new BigDecimal("250.00"));
        request.setCurrency(Currency.USD);
        request.setTransactionType(TransactionType.PAYMENT);
        request.setTransactionStatus(TransactionStatus.PENDING);

        Transaction created = transactionService.createTransaction(request);

        assertNotNull(created);
        assertEquals("TXN_TEST_1", created.getTransactionId());
        assertEquals("CUST_100", created.getCustomerId());
        assertEquals(TransactionStatus.PENDING, created.getTransactionStatus());
    }

    @Test
    void testCreateTransaction_DuplicateId_ThrowsException() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN_DUP_1");
        request.setCustomerId("CUST_100");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency(Currency.INR);
        request.setTransactionType(TransactionType.PAYMENT);

        transactionService.createTransaction(request);

        // Attempt creating duplicate
        assertThrows(DuplicateTransactionException.class, () -> {
            transactionService.createTransaction(request);
        });
    }

    @Test
    void testGetTransactionById_NotFound_ThrowsException() {
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionById("NON_EXISTENT_ID");
        });
    }

    @Test
    void testUpdateTransactionStatus_ValidTransitions() {
        // Create initial PENDING transaction
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN_STATUS_1");
        request.setCustomerId("CUST_200");
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency(Currency.EUR);
        request.setTransactionType(TransactionType.TRANSFER);
        request.setTransactionStatus(TransactionStatus.PENDING);

        transactionService.createTransaction(request);

        // Transition 1: PENDING -> APPROVED
        Transaction approved = transactionService.updateTransactionStatus("TXN_STATUS_1", TransactionStatus.APPROVED);
        assertEquals(TransactionStatus.APPROVED, approved.getTransactionStatus());

        // Transition 2: APPROVED -> COMPLETED
        Transaction completed = transactionService.updateTransactionStatus("TXN_STATUS_1", TransactionStatus.COMPLETED);
        assertEquals(TransactionStatus.COMPLETED, completed.getTransactionStatus());
    }

    @Test
    void testUpdateTransactionStatus_TerminalStatus_ThrowsException() {
        // Create initial COMPLETED transaction
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN_TERM_1");
        request.setCustomerId("CUST_300");
        request.setAmount(new BigDecimal("75.00"));
        request.setCurrency(Currency.USD);
        request.setTransactionType(TransactionType.PAYMENT);
        request.setTransactionStatus(TransactionStatus.COMPLETED);

        transactionService.createTransaction(request);

        // Attempting to change COMPLETED -> APPROVED must fail
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.updateTransactionStatus("TXN_TERM_1", TransactionStatus.APPROVED);
        });

        assertEquals("Transaction status cannot be changed from terminal status: COMPLETED", ex.getMessage());
    }

    @Test
    void testGetTransactionsByCustomerId() {
        CreateTransactionRequest req1 = new CreateTransactionRequest();
        req1.setTransactionId("TXN_CUST_1");
        req1.setCustomerId("CUST_MULTI");
        req1.setAmount(new BigDecimal("50.00"));
        req1.setCurrency(Currency.USD);
        req1.setTransactionType(TransactionType.PAYMENT);

        CreateTransactionRequest req2 = new CreateTransactionRequest();
        req2.setTransactionId("TXN_CUST_2");
        req2.setCustomerId("CUST_MULTI");
        req2.setAmount(new BigDecimal("150.00"));
        req2.setCurrency(Currency.USD);
        req2.setTransactionType(TransactionType.REFUND);

        transactionService.createTransaction(req1);
        transactionService.createTransaction(req2);

        List<Transaction> list = transactionService.getTransactionsByCustomerId("CUST_MULTI");
        assertEquals(2, list.size());
    }
}

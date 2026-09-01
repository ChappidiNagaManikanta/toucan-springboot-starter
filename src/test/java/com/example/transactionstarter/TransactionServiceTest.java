package com.example.transactionstarter;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.enums.Currency;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.enums.TransactionType;
import com.example.transactionstarter.services.TransactionService;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionService transactionService;

    // =========================================================================
    // MANDATORY REQUIREMENT 1: A transaction created successfully
    // =========================================================================
    @Test
    void test1_CreateTransaction_Success() throws Exception {
        String jsonPayload = """
                {
                    "transactionId": "TXN_TEST_101",
                    "customerId": "CUST_99",
                    "amount": 1250.75,
                    "currency": "USD",
                    "transactionType": "PAYMENT",
                    "transactionStatus": "PENDING"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN_TEST_101"))
                .andExpect(jsonPath("$.customerId").value("CUST_99"))
                .andExpect(jsonPath("$.amount").value(1250.75))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.transactionType").value("PAYMENT"))
                .andExpect(jsonPath("$.transactionStatus").value("PENDING"));
    }

    // =========================================================================
    // MANDATORY REQUIREMENT 2: A transaction rejected because it fails validation
    // =========================================================================
    @Test
    void test2_CreateTransaction_ValidationFailure_NegativeAmount() throws Exception {
        String invalidJsonPayload = """
                {
                    "transactionId": "TXN_INVALID_1",
                    "customerId": "CUST_99",
                    "amount": -500.00,
                    "currency": "USD",
                    "transactionType": "PAYMENT",
                    "transactionStatus": "PENDING"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // =========================================================================
    // MANDATORY REQUIREMENT 3: A duplicate Transaction ID rejected
    // =========================================================================
    @Test
    void test3_CreateTransaction_DuplicateId_Rejected() throws Exception {
        String jsonPayload = """
                {
                    "transactionId": "TXN_DUP_REST",
                    "customerId": "CUST_01",
                    "amount": 300.00,
                    "currency": "INR",
                    "transactionType": "TRANSFER",
                    "transactionStatus": "PENDING"
                }
                """;

        // First creation succeeds
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        // Second creation with same ID is rejected with HTTP 409 Conflict
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Transaction already exists with ID: TXN_DUP_REST"));
    }

    // =========================================================================
    // MANDATORY REQUIREMENT 4: A request for a transaction that does not exist
    // =========================================================================
    @Test
    void test4_GetTransaction_NotFound() throws Exception {
        mockMvc.perform(get("/api/transactions/NON_EXISTENT_TXN_ID"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found with ID: NON_EXISTENT_TXN_ID"));
    }

    // =========================================================================
    // ADDITIONAL TEST 5: Get an existing transaction by ID
    // =========================================================================
    @Test
    void test5_GetTransactionById_Success() throws Exception {
        String jsonPayload = """
                {
                    "transactionId": "TXN_GET_BY_ID",
                    "customerId": "CUST_55",
                    "amount": 750.00,
                    "currency": "EUR",
                    "transactionType": "DEPOSIT",
                    "transactionStatus": "PENDING"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transactions/TXN_GET_BY_ID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN_GET_BY_ID"))
                .andExpect(jsonPath("$.amount").value(750.00));
    }

    // =========================================================================
    // ADDITIONAL TEST 6: Update Transaction Status
    // =========================================================================
    @Test
    void test6_UpdateTransactionStatus_Success() throws Exception {
        String createJson = """
                {
                    "transactionId": "TXN_UPDATE_STATUS",
                    "customerId": "CUST_77",
                    "amount": 1000.00,
                    "currency": "GBP",
                    "transactionType": "PAYMENT",
                    "transactionStatus": "PENDING"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        String updateJson = """
                {
                    "status": "APPROVED"
                }
                """;

        mockMvc.perform(patch("/api/transactions/TXN_UPDATE_STATUS/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionStatus").value("APPROVED"));
    }

    // =========================================================================
    // ADDITIONAL TEST 7: Attempt update from Terminal Status (COMPLETED)
    // =========================================================================
    @Test
    void test7_UpdateTransactionStatus_TerminalStatus_Rejected() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setTransactionId("TXN_TERM_SERVICE");
        request.setCustomerId("CUST_300");
        request.setAmount(new BigDecimal("75.00"));
        request.setCurrency(Currency.USD);
        request.setTransactionType(TransactionType.PAYMENT);
        request.setTransactionStatus(TransactionStatus.COMPLETED);

        transactionService.createTransaction(request);

        // Attempting to change COMPLETED -> APPROVED must fail
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.updateTransactionStatus("TXN_TERM_SERVICE", TransactionStatus.APPROVED);
        });

        assertEquals("Transaction status cannot be changed from terminal status: COMPLETED", ex.getMessage());
    }

    // =========================================================================
    // ADDITIONAL TEST 8: Get all transactions for a customer
    // =========================================================================
    @Test
    void test8_GetCustomerTransactions_Success() throws Exception {
        String txn1 = """
                {
                    "transactionId": "TXN_CUST_LIST_1",
                    "customerId": "CUST_ALL",
                    "amount": 100.00,
                    "currency": "USD",
                    "transactionType": "PAYMENT"
                }
                """;
        String txn2 = """
                {
                    "transactionId": "TXN_CUST_LIST_2",
                    "customerId": "CUST_ALL",
                    "amount": 200.00,
                    "currency": "USD",
                    "transactionType": "REFUND"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(txn1)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(txn2)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/customers/CUST_ALL/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // =========================================================================
    // ADDITIONAL TEST 9: Create transaction with space-separated JSON property keys
    // =========================================================================
    @Test
    void test9_CreateTransaction_SpacedJsonKeys_Success() throws Exception {
        String spacedJsonPayload = """
                {
                    "Transaction ID": "TXN_SPACED_202",
                    "Customer ID": "CUST_31275",
                    "Amount": 80000,
                    "Currency": "INR",
                    "Transaction Type": "PAYMENT",
                    "Transaction Status": "COMPLETED"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(spacedJsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN_SPACED_202"))
                .andExpect(jsonPath("$.customerId").value("CUST_31275"))
                .andExpect(jsonPath("$.amount").value(80000))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.transactionType").value("PAYMENT"))
                .andExpect(jsonPath("$.transactionStatus").value("COMPLETED"));
    }
}

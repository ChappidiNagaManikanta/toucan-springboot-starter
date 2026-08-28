package com.example.transactionstarter.sample;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.transactionstarter.dto.CreateTransactionRequest;
import com.example.transactionstarter.dto.UpdateTransactionStatusRequest;
import com.example.transactionstarter.enums.Currency;
import com.example.transactionstarter.enums.TransactionStatus;
import com.example.transactionstarter.enums.TransactionType;
import com.example.transactionstarter.model.Transaction;
import com.example.transactionstarter.services.TransactionService;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SampleController {

    private final TransactionService transactionService;

    public SampleController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // =====================================================
    // INTERACTIVE CONSOLE MENU (Implemented in Controller)
    // =====================================================

    @PostConstruct
    public void initConsoleMenu() {
        Thread thread = new Thread(this::runConsoleMenu);
        thread.setDaemon(true);
        thread.start();
    }

    private void runConsoleMenu() {
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        try {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                System.out.println("\n===============================================");
                System.out.println("     TRANSACTION MANAGEMENT CONSOLE MENU      ");
                System.out.println("===============================================");
                System.out.println(" 1. Create a New Transaction");
                System.out.println(" 2. Get Transaction Details by ID");
                System.out.println(" 3. Update Transaction Status");
                System.out.println(" 4. Get All Transactions for a Customer");
                System.out.println(" 5. Exit Console Menu");
                System.out.println("===============================================");
                System.out.print("Select an option (1-5): ");

                if (!scanner.hasNextLine()) break;
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        handleConsoleCreate(scanner);
                        break;
                    case "2":
                        handleConsoleGetById(scanner);
                        break;
                    case "3":
                        handleConsoleUpdateStatus(scanner);
                        break;
                    case "4":
                        handleConsoleGetByCustomer(scanner);
                        break;
                    case "5":
                        System.out.println("Exiting Console Menu. REST APIs remain active.");
                        return;
                    default:
                        System.out.println("❌ Invalid option. Please enter 1-5.");
                }
            }
        } catch (Exception ignored) {
            // Non-interactive stream (e.g., test suite context), exit console thread cleanly
        }
    }

    private void handleConsoleCreate(Scanner scanner) {
        try {
            System.out.println("\n--- [ 1. CREATE TRANSACTION ] ---");
            System.out.print("Enter Transaction ID (e.g., TXN101): ");
            String txnId = scanner.nextLine().trim();

            System.out.print("Enter Customer ID (e.g., CUST01): ");
            String custId = scanner.nextLine().trim();

            System.out.print("Enter Amount (e.g., 1500.00): ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            System.out.print("Enter Currency (INR, USD, GBP, EUR): ");
            Currency currency = Currency.fromString(scanner.nextLine().trim());

            System.out.print("Enter Type (PAYMENT, REFUND, TRANSFER): ");
            TransactionType type = TransactionType.fromString(scanner.nextLine().trim());

            System.out.print("Enter Status (PENDING, APPROVED, COMPLETED - Press Enter for PENDING): ");
            String statusInput = scanner.nextLine().trim();
            TransactionStatus status = statusInput.isEmpty() ? TransactionStatus.PENDING : TransactionStatus.fromString(statusInput);

            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setTransactionId(txnId);
            request.setCustomerId(custId);
            request.setAmount(amount);
            request.setCurrency(currency);
            request.setTransactionType(type);
            request.setTransactionStatus(status);

            Transaction result = transactionService.createTransaction(request);
            System.out.println("✅ Transaction Created Successfully!");
            printTxn(result);
        } catch (Exception e) {
            System.out.println("❌ Error creating transaction: " + e.getMessage());
        }
    }

    private void handleConsoleGetById(Scanner scanner) {
        try {
            System.out.println("\n--- [ 2. GET TRANSACTION BY ID ] ---");
            System.out.print("Enter Transaction ID: ");
            String txnId = scanner.nextLine().trim();

            Transaction result = transactionService.getTransactionById(txnId);
            System.out.println("✅ Transaction Found:");
            printTxn(result);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void handleConsoleUpdateStatus(Scanner scanner) {
        try {
            System.out.println("\n--- [ 3. UPDATE TRANSACTION STATUS ] ---");
            System.out.print("Enter Transaction ID: ");
            String txnId = scanner.nextLine().trim();

            System.out.print("Enter New Status (COMPLETED, APPROVED, FAILED, CANCELLED): ");
            TransactionStatus status = TransactionStatus.fromString(scanner.nextLine().trim());

            Transaction result = transactionService.updateTransactionStatus(txnId, status);
            System.out.println("✅ Status Updated Successfully!");
            printTxn(result);
        } catch (Exception e) {
            System.out.println("❌ Error updating status: " + e.getMessage());
        }
    }

    private void handleConsoleGetByCustomer(Scanner scanner) {
        try {
            System.out.println("\n--- [ 4. GET CUSTOMER TRANSACTIONS ] ---");
            System.out.print("Enter Customer ID: ");
            String custId = scanner.nextLine().trim();

            List<Transaction> list = transactionService.getTransactionsByCustomerId(custId);
            System.out.println("✅ Found " + list.size() + " transaction(s):");
            for (Transaction t : list) {
                printTxn(t);
            }
        } catch (Exception e) {
            System.out.println("❌ Error retrieving transactions: " + e.getMessage());
        }
    }

    private void printTxn(Transaction t) {
        System.out.println("  ID        : " + t.getTransactionId());
        System.out.println("  Customer  : " + t.getCustomerId());
        System.out.println("  Amount    : " + t.getAmount() + " " + t.getCurrency());
        System.out.println("  Type      : " + t.getTransactionType());
        System.out.println("  Status    : " + t.getTransactionStatus());
        System.out.println("  -----------------------------------");
    }

    // =====================================================
    // REST API ENDPOINTS
    // =====================================================

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(
            @PathVariable String transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @RequestMapping(value = "/transactions/{transactionId}/status", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<Transaction> updateTransactionStatus(
            @PathVariable String transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionService.updateTransactionStatus(transactionId, request.getStatus());
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<List<Transaction>> getCustomerTransactions(
            @PathVariable String customerId) {
        List<Transaction> transactions = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }
}
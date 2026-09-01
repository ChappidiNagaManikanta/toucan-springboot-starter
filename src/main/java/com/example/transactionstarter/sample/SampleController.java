package com.example.transactionstarter.sample;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    // SAMPLE API
    // GET /api/sample
    // =====================================================

    @GetMapping("/sample")
    public Map<String, String> sample() {

        return Map.of(
                "message",
                "Starter project is running"
        );
    }


    // =====================================================
    // 1. CREATE TRANSACTION
    // POST /api/transactions
    // =====================================================

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }


    // =====================================================
    // 2. GET TRANSACTION BY ID
    // GET /api/transactions/{transactionId}
    // =====================================================

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(
            @PathVariable String transactionId) {

        Transaction transaction =
                transactionService.getTransactionById(transactionId);

        return ResponseEntity.ok(transaction);
    }


    // =====================================================
    // 3. UPDATE TRANSACTION STATUS
    // PATCH /api/transactions/{transactionId}/status
    // =====================================================

    @PatchMapping("/transactions/{transactionId}/status")
    public ResponseEntity<Transaction> updateTransactionStatus(
            @PathVariable String transactionId,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {

        Transaction transaction =
                transactionService.updateTransactionStatus(
                        transactionId,
                        request.getTransactionStatus());

        return ResponseEntity.ok(transaction);
    }


    // =====================================================
    // 4. GET ALL TRANSACTIONS FOR CUSTOMER
    // GET /api/customers/{customerId}/transactions
    // =====================================================

    @GetMapping("/customers/{customerId}/transactions")
    public ResponseEntity<List<Transaction>> getCustomerTransactions(
            @PathVariable String customerId) {

        List<Transaction> transactions =
                transactionService.getTransactionsByCustomerId(
                        customerId);

        return ResponseEntity.ok(transactions);
    }

    // =====================================================
    // CONSOLE MENU
    // =====================================================

    @PostConstruct
    public void startConsoleMenu() {

        if (isTestEnvironment()) {
            return;
        }

        Thread consoleThread = new Thread(() -> {

            // Give Spring Boot time to start
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            runConsoleMenu();

        });

        consoleThread.setDaemon(true);
        consoleThread.setName("console-menu-thread");
        consoleThread.start();
    }

    private boolean isTestEnvironment() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.springframework.test.")
                    || element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }


    // =====================================================
    // SWITCH CASE MENU
    // =====================================================

    private void runConsoleMenu() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.println();
            System.out.println("===============================================");
            System.out.println("       TRANSACTION MANAGEMENT SYSTEM");
            System.out.println("===============================================");
            System.out.println("1. Create Transaction");
            System.out.println("2. Get Transaction By ID");
            System.out.println("3. Update Transaction Status");
            System.out.println("4. Get Customer Transactions");
            System.out.println("5. Exit Console");
            System.out.println("===============================================");
            if (!scanner.hasNextLine()) {
                return;
            }

            String choice = scanner.nextLine().trim();

            // Debug line - confirms input is received
            System.out.println("Selected option: " + choice);

            switch (choice) {

                case "1":
                    createTransactionFromConsole(scanner);
                    break;

                case "2":
                    getTransactionFromConsole(scanner);
                    break;

                case "3":
                    updateStatusFromConsole(scanner);
                    break;

                case "4":
                    getCustomerTransactionsFromConsole(scanner);
                    break;

                case "5":
                    System.out.println();
                    System.out.println(
                            "Console menu closed."
                    );
                    System.out.println(
                            "REST APIs are still available."
                    );
                    return;

                default:
                    System.out.println();
                    System.out.println(
                            "Invalid choice!"
                    );
                    System.out.println(
                            "Please enter a number from 1 to 5."
                    );
            }
        }
    }


    // =====================================================
    // OPTION 1 - CREATE
    // =====================================================

    private void createTransactionFromConsole(
            Scanner scanner) {

        try {

            System.out.println();
            System.out.println("-----------------------------------------------");
            System.out.println("        CREATE NEW TRANSACTION");
            System.out.println("-----------------------------------------------");

            System.out.print("Enter Transaction ID: ");
            String transactionId =
                    scanner.nextLine().trim();

            System.out.print("Enter Customer ID: ");
            String customerId =
                    scanner.nextLine().trim();

            System.out.print("Enter Amount: ");
            BigDecimal amount =
                    new BigDecimal(
                            scanner.nextLine().trim()
                    );

            System.out.print(
                    "Enter Currency (INR/USD/GBP/EUR): "
            );

            Currency currency =
                    Currency.valueOf(
                            scanner.nextLine()
                                    .trim()
                                    .toUpperCase()
                    );

            System.out.print(
                    "Enter Transaction Type " +
                    "(PAYMENT/REFUND/TRANSFER): "
            );

            TransactionType transactionType =
                    TransactionType.valueOf(
                            scanner.nextLine()
                                    .trim()
                                    .toUpperCase()
                    );

            System.out.print(
                    "Enter Initial Status " +
                    "(PENDING): "
            );

            String statusInput =
                    scanner.nextLine().trim();

            TransactionStatus status;

            if (statusInput.isEmpty()) {

                status = TransactionStatus.PENDING;

            } else {

                status =
                        TransactionStatus.valueOf(
                                statusInput.toUpperCase()
                        );
            }


            // Create request object
            CreateTransactionRequest request =
                    new CreateTransactionRequest();

            request.setTransactionId(transactionId);
            request.setCustomerId(customerId);
            request.setAmount(amount);
            request.setCurrency(currency);
            request.setTransactionType(transactionType);
            request.setTransactionStatus(status);


            // Call service
            Transaction transaction =
                    transactionService
                            .createTransaction(request);


            System.out.println();
            System.out.println(
                    "Transaction created successfully!"
            );

            printTransaction(transaction);

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }


    // =====================================================
    // OPTION 2 - GET BY ID
    // =====================================================

    private void getTransactionFromConsole(
            Scanner scanner) {

        try {

            System.out.println();
            System.out.println("-----------------------------------------------");
            System.out.println("        GET TRANSACTION BY ID");
            System.out.println("-----------------------------------------------");

            System.out.print(
                    "Enter Transaction ID: "
            );

            String transactionId =
                    scanner.nextLine().trim();


            Transaction transaction =
                    transactionService
                            .getTransactionById(
                                    transactionId
                            );


            System.out.println();
            System.out.println(
                    "Transaction found!"
            );

            printTransaction(transaction);

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }


    // =====================================================
    // OPTION 3 - UPDATE STATUS
    // =====================================================

    private void updateStatusFromConsole(
            Scanner scanner) {

        try {

            System.out.println();
            System.out.println("-----------------------------------------------");
            System.out.println("       UPDATE TRANSACTION STATUS");
            System.out.println("-----------------------------------------------");

            System.out.print(
                    "Enter Transaction ID: "
            );

            String transactionId =
                    scanner.nextLine().trim();


            System.out.print(
                    "Enter New Status " +
                    "(COMPLETED/FAILED/CANCELLED): "
            );

            TransactionStatus status =
                    TransactionStatus.valueOf(
                            scanner.nextLine()
                                    .trim()
                                    .toUpperCase()
                    );


            Transaction transaction =
                    transactionService
                            .updateTransactionStatus(
                                    transactionId,
                                    status
                            );


            System.out.println();
            System.out.println(
                    "Transaction status updated successfully!"
            );

            printTransaction(transaction);

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }


    // =====================================================
    // OPTION 4 - GET CUSTOMER TRANSACTIONS
    // =====================================================

    private void getCustomerTransactionsFromConsole(
            Scanner scanner) {

        try {

            System.out.println();
            System.out.println("-----------------------------------------------");
            System.out.println("       CUSTOMER TRANSACTIONS");
            System.out.println("-----------------------------------------------");

            System.out.print(
                    "Enter Customer ID: "
            );

            String customerId =
                    scanner.nextLine().trim();


            List<Transaction> transactions =
                    transactionService
                            .getTransactionsByCustomerId(
                                    customerId
                            );


            System.out.println();
            System.out.println(
                    "Total Transactions: "
                    + transactions.size()
            );


            if (transactions.isEmpty()) {

                System.out.println(
                        "No transactions found."
                );

            } else {

                for (Transaction transaction :
                        transactions) {

                    printTransaction(transaction);
                }
            }

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }


    // =====================================================
    // PRINT TRANSACTION
    // =====================================================

    private void printTransaction(
            Transaction transaction) {

        System.out.println();
        System.out.println("-----------------------------------------------");

        System.out.println(
                "Transaction ID : "
                + transaction.getTransactionId()
        );

        System.out.println(
                "Customer ID    : "
                + transaction.getCustomerId()
        );

        System.out.println(
                "Amount         : "
                + transaction.getAmount()
        );

        System.out.println(
                "Currency       : "
                + transaction.getCurrency()
        );

        System.out.println(
                "Transaction Type : "
                + transaction.getTransactionType()
        );

        System.out.println(
                "Transaction Status : "
                + transaction.getTransactionStatus()
        );

        System.out.println("-----------------------------------------------");
    }
}
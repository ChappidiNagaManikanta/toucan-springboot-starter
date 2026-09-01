# Transaction Starter Project — Toucan Payments Challenge

## 1. Problem Understanding & Architectural Design

The objective of this project is to build a robust, production-grade RESTful API for managing customer financial transaction lifecycles. The system handles transaction ingestion, retrieval, customer transaction lookup, and strict state-controlled status updates.

### Architectural Principles
* **Layered Architecture:** Clear separation of concerns across Web Controller Layer (`SampleController`), Service Business Logic (`TransactionServiceImpl`), Persistence Layer (`TransactionRepository`), and Entity Data Model (`Transaction`).
* **DTO Separation:** Incoming request payloads are decoupled from the database entities via dedicated Data Transfer Objects (`CreateTransactionRequest`, `UpdateTransactionStatusRequest`). Flexible JSON key aliases (such as `"Transaction ID"` and `"transaction_id"`) are supported via `@JsonAlias`.
* **Financial Precision:** Currency amounts are modeled using `java.math.BigDecimal` to eliminate floating-point rounding errors.
* **Centralized Exception Handling:** Utilizes `@RestControllerAdvice` (`GlobalExceptionHandler`) to deliver structured and consistent JSON error responses.

---

## 2. Assumptions & Data Formats

* **Storage Engine:** In-memory H2 database (`jdbc:h2:mem:transactions`), initialized automatically for testing and execution.
* **Transaction ID:** Unique string identifier (e.g., `TXN_API_101`, `TXN-001`).
* **Customer ID:** Customer identifier string (e.g., `CUST_99`, `CUST-101`).
* **Monetary Amount:** Mandatory positive decimal value strictly greater than `0.00`.
* **Initial State:** Newly created transactions default to `PENDING` status if `transactionStatus` is omitted from the request payload.
* **Supported Currencies (`Currency` Enum):** `INR`, `USD`, `GBP`, `EUR`, `CAD`, `AUD`.
* **Supported Transaction Types (`TransactionType` Enum):** `PAYMENT`, `REFUND`, `TRANSFER`, `DEPOSIT`, `WITHDRAWAL`.
* **Supported Transaction Statuses (`TransactionStatus` Enum):** `PENDING`, `APPROVED`, `PROCESSING`, `COMPLETED`, `SUCCESS`, `FAILED`, `REJECTED`, `CANCELLED`.

---

## 3. Validation Rules & State Machine

### 3.1 Input Validation Matrix

| Field | Type | Validation Constraints | Failure Response |
| :--- | :--- | :--- | :--- |
| `transactionId` | String | `@NotBlank`, Unique ID | `400 Bad Request` / `409 Conflict` (Duplicate) |
| `customerId` | String | `@NotBlank` | `400 Bad Request` |
| `amount` | BigDecimal | `@NotNull`, `@Positive` (> 0.00) | `400 Bad Request` |
| `currency` | Enum | `@NotNull`, Must be valid `Currency` enum | `400 Bad Request` |
| `transactionType` | Enum | `@NotNull`, Must be valid `TransactionType` enum | `400 Bad Request` |
| Idempotency | Business Rule | `transactionId` must be unique | `409 Conflict` |

### 3.2 Transaction State Transition Rules

Status updates are strictly enforced according to valid transitions:

```text
[*] --> PENDING : Transaction Created (Default)
PENDING --> APPROVED / PROCESSING / COMPLETED / SUCCESS / FAILED / REJECTED / CANCELLED
APPROVED --> COMPLETED / SUCCESS / FAILED / CANCELLED
PROCESSING --> COMPLETED / SUCCESS / FAILED / CANCELLED
COMPLETED / SUCCESS / FAILED / REJECTED / CANCELLED --> [*] : Terminal State
```

* **Terminal States:** `COMPLETED`, `SUCCESS`, `FAILED`, `REJECTED`, and `CANCELLED` are immutable terminal states. Any status transition attempt out of these states is rejected with `400 Bad Request`.

---

## 4. REST API Reference

### 4.1 Endpoints Summary

| Method | Endpoint | Description | Success Code | Error Codes |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/transactions` | Create a new transaction | `201 Created` | `400`, `409` |
| **GET** | `/api/transactions/{transactionId}` | Get a specific transaction by ID | `200 OK` | `404` |
| **PATCH**| `/api/transactions/{transactionId}/status` | Update transaction status | `200 OK` | `400`, `404` |
| **GET** | `/api/customers/{customerId}/transactions` | Get all transactions for a customer | `200 OK` | — |
| **GET** | `/api/sample` | Health check endpoint | `200 OK` | — |

---

### 4.2 Endpoint Payloads & Examples

#### 1. Create Transaction (`POST /api/transactions`)
* **Request Body:**
  ```json
  {
    "transactionId": "TXN_101",
    "customerId": "CUST_99",
    "amount": 1250.75,
    "currency": "USD",
    "transactionType": "PAYMENT",
    "transactionStatus": "PENDING"
  }
  ```
* **Response Body (`201 Created`):**
  ```json
  {
    "transactionId": "TXN_101",
    "customerId": "CUST_99",
    "amount": 1250.75,
    "currency": "USD",
    "transactionType": "PAYMENT",
    "transactionStatus": "PENDING"
  }
  ```

#### 2. Get Transaction By ID (`GET /api/transactions/{transactionId}`)
* **Response Body (`200 OK`):**
  ```json
  {
    "transactionId": "TXN_101",
    "customerId": "CUST_99",
    "amount": 1250.75,
    "currency": "USD",
    "transactionType": "PAYMENT",
    "transactionStatus": "PENDING"
  }
  ```

#### 3. Update Transaction Status (`PATCH /api/transactions/{transactionId}/status`)
* **Request Body:**
  ```json
  {
    "status": "APPROVED"
  }
  ```
* **Response Body (`200 OK`):**
  ```json
  {
    "transactionId": "TXN_101",
    "customerId": "CUST_99",
    "amount": 1250.75,
    "currency": "USD",
    "transactionType": "PAYMENT",
    "transactionStatus": "APPROVED"
  }
  ```

#### 4. Get Customer Transactions (`GET /api/customers/{customerId}/transactions`)
* **Response Body (`200 OK`):**
  ```json
  [
    {
      "transactionId": "TXN_101",
      "customerId": "CUST_99",
      "amount": 1250.75,
      "currency": "USD",
      "transactionType": "PAYMENT",
      "transactionStatus": "APPROVED"
    }
  ]
  ```

---

## 5. Error Handling Strategy

Errors are caught by `GlobalExceptionHandler` and returned in a consistent JSON structure:

```json
{
  "error": "Transaction not found with ID: NON_EXISTENT_ID"
}
```

| Exception | HTTP Status | Trigger Condition |
| :--- | :--- | :--- |
| `TransactionNotFoundException` | `404 Not Found` | Transaction ID not found |
| `DuplicateTransactionException`| `409 Conflict` | Attempt to register an existing `transactionId` |
| `MethodArgumentNotValidException` | `400 Bad Request` | Missing required fields or negative amount validation failure |
| `HttpMessageNotReadableException` | `400 Bad Request` | Malformed JSON payload or invalid Enum value |
| `IllegalArgumentException` | `400 Bad Request` | Invalid status transition or attempt to modify terminal status |

---

## 6. Testing Strategy & Execution

Automated unit and integration tests are implemented in a single consolidated test file `TransactionServiceTest` using **JUnit 5**, **Spring Boot Test**, and **MockMvc**.

### Test Suite (`TransactionServiceTest`):
1. `test1_CreateTransaction_Success`: Validates successful creation (`201 Created`).
2. `test2_CreateTransaction_ValidationFailure_NegativeAmount`: Confirms negative amounts are rejected (`400 Bad Request`).
3. `test3_CreateTransaction_DuplicateId_Rejected`: Confirms duplicate `transactionId` is rejected (`409 Conflict`).
4. `test4_GetTransaction_NotFound`: Confirms searching non-existent transaction returns `404 Not Found`.
5. `test5_GetTransactionById_Success`: Verifies exact ID lookup.
6. `test6_UpdateTransactionStatus_Success`: Verifies valid status transition (`PENDING` ➔ `APPROVED`).
7. `test7_UpdateTransactionStatus_TerminalStatus_Rejected`: Verifies illegal transition from terminal state (`COMPLETED`) throws exception.
8. `test8_GetCustomerTransactions_Success`: Verifies retrieval of transactions by `customerId`.
9. `test9_CreateTransaction_SpacedJsonKeys_Success`: Verifies compatibility with space-separated JSON keys (`"Transaction ID"`).

### Running Tests
* **Windows:**
  ```bat
  .\mvnw.cmd test
  ```
* **Linux / macOS:**
  ```bash
  ./mvnw test
  ```

---

## 7. Known Limitations

* **In-Memory Volatility:** H2 stores data in RAM (`jdbc:h2:mem:transactions`); data resets when the application stops.
* **Authentication:** API endpoints are unauthenticated for challenge evaluation purposes.
* **Console Interface:** Embedded interactive console menu is active when running locally (`SampleController`), but automatically disabled during test executions.

---

## 8. Future Improvements

1. **Persistent Database:** Migrate from H2 to PostgreSQL / MySQL with Flyway database migration scripts.
2. **Pagination:** Add Spring Data `Pageable` parameters to `/api/customers/{customerId}/transactions`.
3. **OpenAPI / Swagger:** Add `springdoc-openapi` for UI documentation.
4. **Auditing:** Include audit timestamps (`createdAt`, `updatedAt`).

---

## 9. AI Usage Disclosure

AI tools were used during development and review to assist with understanding Spring Boot concepts, reviewing the application structure, identifying potential implementation issues, and improving validation, exception handling, testing, and API design.

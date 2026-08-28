# Transaction Management Interactive Console Script
$baseUrl = "http://localhost:8080/api"

function Show-Menu {
    Clear-Host
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host "     TRANSACTION MANAGEMENT CONSOLE MENU      " -ForegroundColor Yellow
    Write-Host "===============================================" -ForegroundColor Cyan
    Write-Host " 1. Create a New Transaction"
    Write-Host " 2. Get Transaction Details by ID"
    Write-Host " 3. Update Transaction Status"
    Write-Host " 4. Get All Transactions for a Customer"
    Write-Host " 5. Exit"
    Write-Host "===============================================" -ForegroundColor Cyan
}

do {
    Show-Menu
    $choice = Read-Host "Select an option (1-5)"

    switch ($choice) {
        "1" {
            Write-Host "`n--- [ 1. CREATE TRANSACTION ] ---" -ForegroundColor Green
            $txnId = Read-Host "Enter Transaction ID (e.g., TXN101)"
            $custId = Read-Host "Enter Customer ID (e.g., CUST01)"
            $amount = Read-Host "Enter Amount (e.g., 1500.00)"
            $currency = Read-Host "Enter Currency (INR, USD, GBP, EUR)"
            $type = Read-Host "Enter Type (PAYMENT, REFUND, TRANSFER)"
            $status = Read-Host "Enter Status (PENDING, COMPLETED, APPROVED)"

            $body = @{
                transactionId     = $txnId
                customerId        = $custId
                amount            = [decimal]$amount
                currency          = $currency.ToUpper()
                transactionType   = $type.ToUpper()
                transactionStatus = $status.ToUpper()
            } | ConvertTo-Json

            try {
                $response = Invoke-RestMethod -Uri "$baseUrl/transactions" -Method POST -ContentType "application/json" -Body $body
                Write-Host "`n✅ Transaction Created Successfully!" -ForegroundColor Green
                $response | Format-List
            } catch {
                Write-Host "`n❌ Error creating transaction: $_" -ForegroundColor Red
            }
            Pause
        }

        "2" {
            Write-Host "`n--- [ 2. GET TRANSACTION BY ID ] ---" -ForegroundColor Green
            $txnId = Read-Host "Enter Transaction ID"
            try {
                $response = Invoke-RestMethod -Uri "$baseUrl/transactions/$txnId" -Method GET
                Write-Host "`n✅ Transaction Found:" -ForegroundColor Green
                $response | Format-List
            } catch {
                Write-Host "`n❌ Transaction Not Found!" -ForegroundColor Red
            }
            Pause
        }

        "3" {
            Write-Host "`n--- [ 3. UPDATE TRANSACTION STATUS ] ---" -ForegroundColor Green
            $txnId = Read-Host "Enter Transaction ID"
            $newStatus = Read-Host "Enter New Status (COMPLETED, APPROVED, FAILED, CANCELLED)"

            $body = @{
                status = $newStatus.ToUpper()
            } | ConvertTo-Json

            try {
                $response = Invoke-RestMethod -Uri "$baseUrl/transactions/$txnId/status" -Method PATCH -ContentType "application/json" -Body $body
                Write-Host "`n✅ Status Updated Successfully!" -ForegroundColor Green
                $response | Format-List
            } catch {
                Write-Host "`n❌ Error updating status: $_" -ForegroundColor Red
            }
            Pause
        }

        "4" {
            Write-Host "`n--- [ 4. GET CUSTOMER TRANSACTIONS ] ---" -ForegroundColor Green
            $custId = Read-Host "Enter Customer ID"
            try {
                $response = Invoke-RestMethod -Uri "$baseUrl/customers/$custId/transactions" -Method GET
                Write-Host "`n✅ Transactions Found:" -ForegroundColor Green
                $response | Format-Table -AutoSize
            } catch {
                Write-Host "`n❌ Error retrieving transactions: $_" -ForegroundColor Red
            }
            Pause
        }

        "5" {
            Write-Host "`nExiting Console. Goodbye!" -ForegroundColor Yellow
        }

        default {
            Write-Host "`nInvalid option. Please try again." -ForegroundColor Red
            Pause
        }
    }
} while ($choice -ne "5")

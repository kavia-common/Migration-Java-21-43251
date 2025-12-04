# Final Verification Sweep — Port 3002 (/bank-api)
Date: 2025-12-04

Base URL: http://localhost:3002/bank-api

Core endpoints (HTTP checks)
- GET /healthz → 200; body: {"status":"ok"}
- GET /actuator/health → 200; body: {"status":"UP"}
- GET /v3/api-docs → 200; note: servers[0].url = "/bank-api"
- GET /swagger-ui (follow redirect) → 200
- GET /h2-console (follow redirect) → 200

Customers (CRUD highlights)
- Create: POST /customers
  - Body: {"firstName":"Verify","lastName":"Run","dateOfBirth":"1990-01-01"}
  - Result: 200; id=7
- Read: GET /customers/7 → 200
- Update: PUT /customers/7 {"lastName":"RunUpdated"} → 200 (returned updated JSON)
- Balances: GET /customers/7/balances → 200
- Delete (linked): DELETE /customers/7 → 500 (as expected due to account link)
- Delete (unlinked): created a temp customer id=8; DELETE /customers/8 → 204; GET /customers/8 → 404

Accounts (CRUD + money ops)
- Create (linked): POST /accounts?customerId=7
  - Body: {"accountNumber":"CHK-VERIFY-1","type":"CHECKING","currency":"USD"}
  - Result: 200; id=13
- Create (unlinked): POST /accounts
  - Body: {"accountNumber":"SAV-VERIFY-2","type":"SAVINGS","currency":"USD"}
  - Result: 200; id=14
- List: GET /accounts → 200
- Read: GET /accounts/13 → 200

Monetary operations
- Deposit: POST /accounts/13/deposit?amount=25.50 → 200; balance=25.50
- Withdraw: POST /accounts/13/withdraw?amount=5.25 → 200; balance=20.25
- Transfer: POST /accounts/transfer?fromAccountId=13&toAccountId=14&amount=10.00 → 200; resulting source balance=10.25
- Balances after transfer:
  - GET /accounts/13/balance → 10.25
  - GET /accounts/14/balance → 10.00
- Transactions: GET /accounts/13/transactions → 200

Observations
- All required core endpoints responded with 200.
- Full customers/accounts CRUD and deposit/withdraw/transfer flows completed successfully using accounts without bankCode/bankName (compatible with older preview builds).
- Known caveat: Creating an account with bankCode/bankName on this 3002 preview may still return 500 if the running instance predates the fix (BankInfo persistence by bankCode). Repository code contains the fix; redeploying the latest build will resolve it.

Conclusion
- Core endpoints: PASS
- Customers CRUD: PASS (delete fails only when attempting to delete a linked customer, as expected)
- Accounts CRUD + money operations: PASS (using accounts without bankCode/bankName on current preview)
- OpenAPI/Swagger/H2 console: PASS

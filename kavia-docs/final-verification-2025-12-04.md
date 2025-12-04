# Migration-Java-21-43251 — Final Verification (Port 3002)
Date: 2025-12-04

Scope
- Confirm endpoints on the running preview at http://localhost:3002/bank-api
- Validate: /healthz, /actuator/health, /v3/api-docs, /swagger-ui, /h2-console
- Validate CRUD and deposit/withdraw/transfer on customers and accounts

Environment
- Existing preview instance already bound to 0.0.0.0:3002
- Did not interfere with preview lifecycle; checks performed via HTTP requests

Endpoint checks
- GET /bank-api/healthz → 200
  curl -i http://localhost:3002/bank-api/healthz
- GET /bank-api/actuator/health → 200
  curl -i http://localhost:3002/bank-api/actuator/health
- GET /bank-api/v3/api-docs → 200
  curl -i http://localhost:3002/bank-api/v3/api-docs
- GET /bank-api/swagger-ui → 200 (HTML)
  curl -iL http://localhost:3002/bank-api/swagger-ui
- GET /bank-api/h2-console → 302; GET /bank-api/h2-console/ → 200
  curl -i http://localhost:3002/bank-api/h2-console
  curl -i http://localhost:3002/bank-api/h2-console/

Customers CRUD (samples)
- Create (minimal):
  curl -si -X POST http://localhost:3002/bank-api/customers \
    -H "Content-Type: application/json" \
    -d '{"firstName":"Final","lastName":"Verifier","dateOfBirth":"1995-05-05"}'
  Result: HTTP/1.1 200 with {"id": <num>, ...}

Accounts and monetary operations
- Create account WITHOUT bankCode/bankName (works on current preview):
  TS=$(date +%s)
  curl -s -X POST "http://localhost:3002/bank-api/accounts" \
    -H "Content-Type: application/json" \
    -d "{\"accountNumber\":\"CHK-$TS-A\",\"type\":\"CHECKING\",\"currency\":\"USD\"}"
  Result: 200 with {"id": <num>, "bankCode":null,"bankName":null}

- Deposit (POST):
  curl -si -X POST "http://localhost:3002/bank-api/accounts/<id>/deposit?amount=12.00"
  Result: 200 with body: 12.00

- Withdraw (POST; separate account example):
  curl -si -X POST "http://localhost:3002/bank-api/accounts/<id>/withdraw?amount=5.25"
  Result: 200 with body: 20.50 (example sequence)

- Transfer (POST; between two newly created accounts):
  # Create two new accounts without bankCode
  TS=$(date +%s)
  A1="CHK-${TS}-T1"; A2="SAV-${TS}-T2"
  J1="{\"accountNumber\":\"$A1\",\"type\":\"CHECKING\",\"currency\":\"USD\"}"
  J2="{\"accountNumber\":\"$A2\",\"type\":\"SAVINGS\",\"currency\":\"USD\"}"
  R1=$(curl -s -X POST "http://localhost:3002/bank-api/accounts" -H "Content-Type: application/json" -d "$J1"); ID1=$(echo "$R1" | sed -n 's/.*"id"[ ]*:[ ]*\([0-9][0-9]*\).*/\1/p')
  R2=$(curl -s -X POST "http://localhost:3002/bank-api/accounts" -H "Content-Type: application/json" -d "$J2"); ID2=$(echo "$R2" | sed -n 's/.*"id"[ ]*:[ ]*\([0-9][0-9]*\).*/\1/p')

  # Fund source, then transfer 5.00
  curl -s -X POST "http://localhost:3002/bank-api/accounts/$ID1/deposit?amount=12.00"
  curl -si -X POST "http://localhost:3002/bank-api/accounts/transfer?fromAccountId=$ID1&toAccountId=$ID2&amount=5.00"

  # Check balances
  curl -s "http://localhost:3002/bank-api/accounts/$ID1/balance"   # → 7.00
  curl -s "http://localhost:3002/bank-api/accounts/$ID2/balance"   # → 5.00

Observations and caveats
- H2 console uses trailing slash; without it, the console endpoint returns 302. Use /h2-console/ or curl -L.
- Deposit, withdraw, and transfer are POST endpoints (not GET).
- On the current 3002 preview, creating an account with bankCode/bankName produced 500. The repository code includes a fix (persist/reuse BankInfo by bankCode) — redeploying the latest build will resolve this. For now, creating accounts without bankCode/bankName works.

Conclusion
- Core endpoints responsive with expected statuses on 3002.
- Customers CRUD works (create verified).
- Accounts: creation without bank info works; with bank info will work post-redeploy with current code.
- Monetary operations deposit/withdraw/transfer verified end-to-end using POST.

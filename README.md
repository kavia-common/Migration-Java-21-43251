# Migration-Java-21-43251: Bank API (Java 21, Spring Boot 3)

This project is a Java 21 Spring Boot 3 migration container that mirrors the features and behavior of BankApp-179898 while keeping the original project untouched. It provides RESTful endpoints for customers, accounts, balances, deposits, withdrawals, transfers, transactions, and OpenAPI documentation, backed by an in-memory H2 database.

## Project location and purpose

- Project path: /home/kavia/workspace/code-generation/Migration-Java-21-43251
- Container name: BankApp-Java21
- Purpose: Provide a Java 21, Spring Boot 3 runtime that achieves endpoint and behavior parity with BankApp-179898, without modifying the original codebase.
- Guarantee: The original BankApp-179898 codebase remains untouched. This Java 21 project was created separately to achieve runtime and feature parity.

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9.x or newer
- Ability to bind TCP port 3002 on 0.0.0.0

Verify your environment:
- java -version
- mvn -version

## Build

Build from the repository root using the project POM:

```bash
# From repo root
mvn -q -DskipTests -f Migration-Java-21-43251/pom.xml clean package

# Run tests
mvn -q -f Migration-Java-21-43251/pom.xml test
```

Notes:
- The project compiles with Java 21 (maven-compiler-plugin uses <release>21</release>).
- Packaging produces a JAR at Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar.

## How to run (port 3002, context-path /bank-api)

Default runtime configuration comes from application.yml:
- Port: 3002 (configurable via PORT env var)
- Address: 0.0.0.0
- Context path: /bank-api

Run via Maven (from repo root):

```bash
mvn -f Migration-Java-21-43251/pom.xml spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=3002 --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"
```

Run from the project directory:

```bash
cd Migration-Java-21-43251
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=3002 --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"
```

Run the packaged JAR (after mvn package):

```bash
java -jar Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar
# Overrides (optional):
# java -jar Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar \
#   --server.port=3002 --server.address=0.0.0.0 --server.servlet.context-path=/bank-api
# Or using env:
# PORT=3002 java -jar Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar
```

Security note: For development and smoke testing, endpoints are publicly accessible (CSRF disabled; frame options set to sameOrigin for H2 console) per SecurityConfig.

## Endpoints quick reference

All paths are relative to the context path /bank-api.

- GET /healthz — Simple health check
- GET /actuator/health — Spring Boot Actuator health
- GET /v3/api-docs — OpenAPI document (JSON)
- GET /swagger-ui — Swagger UI (redirects to /swagger-ui/index.html)
- GET /h2-console — H2 console (302 to /h2-console/; use trailing slash)

Quick curl checks:

```bash
curl -i http://localhost:3002/bank-api/healthz
curl -i http://localhost:3002/bank-api/actuator/health
curl -i http://localhost:3002/bank-api/v3/api-docs
curl -i http://localhost:3002/bank-api/swagger-ui
curl -iL http://localhost:3002/bank-api/h2-console
```

Swagger UI in a browser:
- http://localhost:3002/bank-api/swagger-ui
- http://localhost:3002/bank-api/swagger-ui/index.html

## CRUD and money operations examples

All examples assume the service is running at http://localhost:3002/bank-api and no authentication is required.

### Customers

Create a customer:

```bash
curl -s -X POST http://localhost:3002/bank-api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Anderson",
    "dateOfBirth": "1990-01-10",
    "address": {
      "line1": "123 Main St",
      "line2": "",
      "city": "Springfield",
      "state": "CA",
      "zip": "90210",
      "country": "USA"
    },
    "contact": {
      "phone": "555-111-2222",
      "email": "alice@example.com"
    }
  }' | jq .
```

List customers:

```bash
curl -s http://localhost:3002/bank-api/customers | jq .
```

Get a customer by id (replace 1 with the returned id):

```bash
curl -s http://localhost:3002/bank-api/customers/1 | jq .
```

Update a customer:

```bash
curl -s -X PUT http://localhost:3002/bank-api/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Andrews",
    "address": {
      "line1": "123 Main St",
      "city": "Springfield",
      "state": "CA",
      "zip": "90210",
      "country": "USA"
    },
    "contact": {
      "phone": "555-222-3333",
      "email": "alice@example.com"
    }
  }' | jq .
```

Delete a customer:

```bash
curl -i -X DELETE http://localhost:3002/bank-api/customers/1
```

Get balances for all accounts linked to a customer:

```bash
curl -s http://localhost:3002/bank-api/customers/1/balances | jq .
```

### Accounts and monetary operations

Create an account (optionally link to an existing customerId):

```bash
# Create an account not linked to any customer
curl -s -X POST "http://localhost:3002/bank-api/accounts" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "CHK-10001",
    "type": "CHECKING",
    "currency": "USD",
    "bankCode": "BK100",
    "bankName": "Bank One"
  }' | jq .

# Create and link to customer id=1
curl -s -X POST "http://localhost:3002/bank-api/accounts?customerId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "SAV-20001",
    "type": "SAVINGS",
    "currency": "USD",
    "bankCode": "BK200",
    "bankName": "Bank Two"
  }' | jq .
```

List accounts:

```bash
curl -s http://localhost:3002/bank-api/accounts | jq .
```

Get account by id (replace 1 with the returned id):

```bash
curl -s http://localhost:3002/bank-api/accounts/1 | jq .
```

Update account metadata:

```bash
curl -s -X PUT http://localhost:3002/bank-api/accounts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "CHK-10001",
    "type": "CHECKING",
    "currency": "USD",
    "bankCode": "BK100",
    "bankName": "Bank One (Updated)"
  }' | jq .
```

Get current balance:

```bash
curl -s http://localhost:3002/bank-api/accounts/1/balance
```

Deposit funds:

```bash
curl -s -X POST "http://localhost:3002/bank-api/accounts/1/deposit?amount=100.50"
```

Withdraw funds:

```bash
curl -s -X POST "http://localhost:3002/bank-api/accounts/1/withdraw?amount=20.00"
```

Transfer funds:

```bash
# Transfer 10.00 from account 1 to account 2
curl -s -X POST "http://localhost:3002/bank-api/accounts/transfer?fromAccountId=1&toAccountId=2&amount=10.00"
```

Get transaction history:

```bash
curl -s http://localhost:3002/bank-api/accounts/1/transactions | jq .
```

## H2 console notes (frame options, path, trailing slash)

- Console URL: http://localhost:3002/bank-api/h2-console/ (note the trailing slash; /h2-console redirects with 302)
- Frame options are set to sameOrigin in SecurityConfig to allow the H2 console UI to render.
- JDBC URL: jdbc:h2:mem:bankdb
- Username: sa
- Password: (leave blank)
- The H2 database is in-memory and lives for the JVM lifetime (DB_CLOSE_DELAY=-1).

Tip: With curl, use -L to follow the 302 redirect:
```bash
curl -iL http://localhost:3002/bank-api/h2-console
```

## Seed profile (optional) and idempotence

A seeding profile is available to preload example customers and accounts. It runs only when the seed profile is active and is idempotent:
- Customers are located by name/date-of-birth before insert.
- Accounts are located by unique accountNumber before insert.
- XRefs are only created if the link does not exist.
- Initial deposit transactions are created only when the account has no transactions.

Enable the seed profile:

```bash
# Maven run with seed profile (from repo root)
mvn -f Migration-Java-21-43251/pom.xml spring-boot:run -Dspring-boot.run.profiles=seed

# Running the JAR with seed profile
java -jar Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar --spring.profiles.active=seed
```

## Parity confirmation with BankApp-179898

This Java 21 service provides endpoint and behavior parity with BankApp-179898. The following capabilities are confirmed:
- CRUD operations for customers and accounts
- Monetary operations: deposit, withdraw, transfer, and transaction history
- Balance retrieval and customer-linked balances
- In-memory H2 database with web console enabled
- OpenAPI documentation via springdoc-openapi under /bank-api/swagger-ui
- Spring Boot Actuator health endpoint under /bank-api/actuator/health

Original code remains untouched: BankApp-179898 was not modified. This Java 21 runtime lives separately at /home/kavia/workspace/code-generation/Migration-Java-21-43251.

## Verification logs (2025-12-04)

Verification artifacts demonstrating parity and smoke checks:
- kavia-docs/smoke-check-2025-12-04.md
- kavia-docs/endpoint-verification-logs-2025-12-04.md
- kavia-docs/final-verification-2025-12-04.md
- kavia-docs/final-verification-sweep-2025-12-04-3002.md
- kavia-docs/java21-ci-commands-and-setup.md
- kavia-docs/java21-migration-modernization-guide.md

Open directly from the repository for details on the executed checks.

## Troubleshooting

- If Swagger UI does not load, ensure you are using the context path (/bank-api) and the correct port (3002).
- If H2 console prompts for connection:
  - JDBC URL: jdbc:h2:mem:bankdb
  - User: sa
  - Password: (leave blank)
- If curl shows connection errors, verify the service is running and bound to 0.0.0.0:3002, and no firewall rules block localhost access.
- For browser-origin calls (e.g., web UIs), CORS is enabled with allowed origins configurable via the ALLOWED_ORIGINS property.

## License

See the root project license in the repository if applicable.

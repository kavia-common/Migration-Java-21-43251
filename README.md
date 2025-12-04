# Migration-Java-21-43251: Bank API (Java 21, Spring Boot 3)

This project is a Java 21 Spring Boot 3 migration container that mirrors the features and behavior of BankApp-179898 while keeping the original project untouched. It provides RESTful endpoints for customers, accounts, balances, deposits, withdrawals, transfers, transactions, and OpenAPI documentation, backed by an in-memory H2 database.

Location and guarantees:
- Project path: /home/kavia/workspace/code-generation/Migration-Java-21-43251
- The original BankApp-179898 codebase remains untouched. This Java 21 project was created separately to achieve runtime and feature parity.

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9.x or newer
- Network access to bind to port 3002 (default) on 0.0.0.0

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

## Run

Default runtime configuration:
- Port: 3002
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
```

Security note: For development and smoke testing, all endpoints are publicly accessible (CSRF disabled, frame options relaxed for H2 console).

## Quick smoke-check endpoints

Once the app is running on port 3002 with context path /bank-api, these should all respond with HTTP 200 without authentication:

```bash
# Health
curl -i http://localhost:3002/bank-api/healthz

# Actuator health
curl -i http://localhost:3002/bank-api/actuator/health

# OpenAPI JSON
curl -i http://localhost:3002/bank-api/v3/api-docs

# Swagger UI (redirects to /bank-api/swagger-ui/index.html)
curl -i http://localhost:3002/bank-api/swagger-ui

# H2 Console (HTML)
curl -i http://localhost:3002/bank-api/h2-console
```

Swagger UI in a browser:
- http://localhost:3002/bank-api/swagger-ui
- http://localhost:3002/bank-api/swagger-ui/index.html

## API overview and parity

This Java 21 service mirrors BankApp-179898 capabilities:
- CRUD operations for customers and accounts.
- Monetary operations: deposit, withdraw, transfer.
- Balances and transaction history.
- In-memory H2 database with web console enabled.
- OpenAPI documentation via springdoc-openapi under /bank-api/swagger-ui.
- Spring Boot Actuator health endpoint under /bank-api/actuator/health.

Controllers and routes:
- HealthController: GET /bank-api/health and /bank-api/healthz
- CustomerController: /bank-api/customers
- AccountController: /bank-api/accounts (incl. balance, deposit, withdraw, transfer, transactions)

## H2 database console

- Console: http://localhost:3002/bank-api/h2-console
- Default JDBC URL: jdbc:h2:mem:bankdb
- Username: sa
- Password: (leave blank)

The database is in-memory and persists only while the JVM is running. The configuration keeps the database alive for the application lifetime (DB_CLOSE_DELAY=-1).

## Seed sample data (optional)

A seeding profile is available to preload example customers and accounts. It runs only when the seed profile is active.

Enable the seed profile:

```bash
# Maven run with seed profile (from repo root)
mvn -f Migration-Java-21-43251/pom.xml spring-boot:run -Dspring-boot.run.profiles=seed

# Running the JAR with seed profile
java -jar Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar --spring.profiles.active=seed
```

The seed profile creates example customers (with address/contact), accounts with initial balances, links them, and records initial deposit transactions.

## Example usage with curl

All examples assume the service is running at http://localhost:3002/bank-api and no authentication is required.

### Health and docs

```bash
curl -s http://localhost:3002/bank-api/health    | jq .
curl -s http://localhost:3002/bank-api/healthz   | jq .
curl -s http://localhost:3002/bank-api/actuator/health | jq .
curl -s http://localhost:3002/bank-api/v3/api-docs     | jq . | head
```

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

## Configuration highlights

- server.port: 3002
- server.servlet.context-path: /bank-api
- spring.h2.console.enabled: true (path: /h2-console under the app context)
- springdoc-openapi: Swagger UI at /bank-api/swagger-ui; OpenAPI JSON at /bank-api/v3/api-docs
- Actuator: /bank-api/actuator/health exposed (no auth)
- Global CORS: Allowed origins can be configured with ALLOWED_ORIGINS (WebCorsConfig).

## Parity statement

- This Java 21 project mirrors the REST endpoints, behaviors, and developer experience of BankApp-179898:
  - CRUD for customers and accounts
  - Deposits, withdrawals, transfers, balances, and transaction history
  - H2 in-memory persistence with console access
  - OpenAPI documentation and Swagger UI
  - Actuator health checks
- The original project was not modified. This Java 21 runtime lives separately at /home/kavia/workspace/code-generation/Migration-Java-21-43251.

## Troubleshooting

- If Swagger UI does not load, ensure you are using the context-path (/bank-api) and the correct port (3002).
- If H2 console prompts for connection:
  - JDBC URL: jdbc:h2:mem:bankdb
  - User: sa
  - Password: (leave blank)
- If curl shows connection errors, verify the service is running and bound to 0.0.0.0:3002, and no firewall rules block localhost access.

## License

See the root project license in the repository if applicable.

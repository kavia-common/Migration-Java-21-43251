# Migration-Java-21

This workspace contains a Java 21 Spring Boot 3 scaffold for BankApp-Java21 using Maven Wrapper.

Build and version check:
- Linux/macOS: `chmod +x mvnw && ./mvnw -q -e --version`
- Windows: `mvnw.cmd -q -e --version`

Package (skipping tests for now):
- Linux/macOS: `./mvnw -q -DskipTests package`
- Windows: `mvnw.cmd -q -DskipTests package`

Run:
- Linux/macOS: `./mvnw spring-boot:run`
- Windows: `mvnw.cmd spring-boot:run`

After start, health and OpenAPI endpoints:
- Actuator health: `GET http://localhost:3002/bank-api/actuator/health`
- Simple REST health: `GET http://localhost:3002/bank-api/health` (returns `{"status":"UP"}`)
- Swagger UI (springdoc): `GET http://localhost:3002/bank-api/swagger-ui` (redirects to `/bank-api/swagger-ui/index.html`)
- OpenAPI JSON: `GET http://localhost:3002/bank-api/v3/api-docs`

Preview/remote environment quick links:
- Health: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/health
- Actuator health: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/actuator/health
- Swagger UI: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/swagger-ui
- OpenAPI JSON: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/v3/api-docs

## CI Commands (Step 2.3)

Use the following exact commands in CI:

- Verify Java/Maven Wrapper:
  - `chmod +x mvnw && ./mvnw -q -e --version`

- Build (skip tests for faster CI in scaffold stage):
  - `./mvnw -q -DskipTests package`

- Optional: Run (if needed in a preview job/stage):
  - `./mvnw spring-boot:run`

Note:
- The application context path is `/bank-api`, so Actuator health is available at `/bank-api/actuator/health`.
- Spring Boot Actuator dependency is present in `pom.xml`, and `application.yml` exposes `health` and `info` endpoints.

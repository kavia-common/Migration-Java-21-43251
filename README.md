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

After start, health endpoint:
- `GET http://localhost:3002/bank-api/actuator/health`
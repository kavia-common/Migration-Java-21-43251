# Migration-Java-21

This workspace contains a Java 21 Spring Boot 3 scaffold for BankApp-Java21 using system Maven (no wrapper assumptions).

Prerequisites:
- JDK 21 (Temurin recommended)
- Maven 3.9.x

Version check:
- `java -version`
- `mvn -version`

Package (skipping tests for now):
- From this directory: `mvn -q -DskipTests clean package`
- From repo root (using -f): `mvn -q -DskipTests -f Migration-Java-21-43251/pom.xml clean package`

Run:
- From this directory:
  `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT:-3002} --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"`
- From repo root (using -f):
  `mvn -f Migration-Java-21-43251/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT:-3002} --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"`

Jar execution (after packaging):
- `java -jar target/bankapp-java21-0.0.1-SNAPSHOT.jar`

After start, health and OpenAPI endpoints (context path is /bank-api):
- Actuator health: GET http://localhost:3002/bank-api/actuator/health
- Simple REST health: GET http://localhost:3002/bank-api/health (returns {"status":"UP"})
- Swagger UI (springdoc): GET http://localhost:3002/bank-api/swagger-ui (redirects to /bank-api/swagger-ui/index.html)
- OpenAPI JSON: GET http://localhost:3002/bank-api/v3/api-docs

Preview/remote environment quick links:
- Health: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/health
- Actuator health: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/actuator/health
- Swagger UI: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/swagger-ui
- OpenAPI JSON: https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002/bank-api/v3/api-docs

Notes:
- Java 21 compilation is enforced via maven-compiler-plugin with <release>21</release>.
- springdoc-openapi v2 starter is used with a relative OpenAPI server URL equal to the context path, ensuring HTTPS-friendly behavior behind proxies.
- Global CORS is enabled for both MVC and Actuator; configure ALLOWED_ORIGINS env var if needed:
  export ALLOWED_ORIGINS="https://your-preview-host:3000,http://localhost:3000"
- Actuator health and Swagger UI are served under /bank-api.

## CI Commands (system Maven; no wrapper)

Use absolute or repo-relative -f to avoid working-directory issues:

- Build (skip tests):
  mvn -q -DskipTests -f Migration-Java-21-43251/pom.xml clean package

- Test:
  mvn -q -f Migration-Java-21-43251/pom.xml test

- Optional: Run (in a preview job/stage):
  mvn -f Migration-Java-21-43251/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=3002 --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"

Additional details and CI examples are available in kavia-docs/java21-migration-modernization-guide.md and kavia-docs/java21-ci-commands-and-setup.md.

# Migration to Java 21: Success & Modernization Report

## Executive Summary

The migration of the banking REST API to a dedicated Java 21 runtime (BankApp-Java21) has been successfully completed in the Migration-Java-21-43251 workspace without altering the original BankApp-179898 project. The Java 21 service runs on port 3002 under the context path /bank-api and provides parity for customer/account CRUD, balances, deposits, withdrawals, transfers, transactions history, health checks, OpenAPI documentation, and the H2 console. Multiple verification passes confirm successful build and runtime behavior. A known caveat was observed on an older preview instance where creating accounts with bank bankCode/bankName returned HTTP 500; the fix to persist/reuse BankInfo by bankCode is present in this repository and verified to work when the latest build is run. Redeploying the 3002 preview with the current build aligns observed runtime with the verified results.

References:
- See README for run/build guidance and endpoint quick reference.
- See verification logs: smoke-check-2025-12-04.md, endpoint-verification-logs-2025-12-04.md, final-verification-2025-12-04.md, and final-verification-sweep-2025-12-04-3002.md.

## Scope and Constraints

This migration intentionally leaves the original application untouched. The Java 21 target is a separate Spring Boot 3 codebase that mirrors the original features and endpoints.

- Original app: BankApp-179898 (default port 3001; context-path /bank-api). Not modified as part of this work.
- Migrated app: BankApp-Java21 in Migration-Java-21-43251 (default port 3002; context-path /bank-api).
- Networking and context:
  - Address: 0.0.0.0
  - Port: 3002 (overridable via PORT)
  - Context path: /bank-api
- Persistence: In-memory H2 for development and tests with optional seeding profile (“seed”).
- Security: Permissive development posture using Spring Security 6.3-compatible configuration; production hardening is recommended.

## Detailed Verification Results

The following summarizes what was verified and how, with outcomes and notes. The results are based on the kavia-docs verification artifacts and the application source.

| Area | What was verified | Method / Source | Result | Notes |
| --- | --- | --- | --- | --- |
| Build & Packaging | Java 21 compilation, Spring Boot 3 packaging | mvn -f Migration-Java-21-43251/pom.xml clean package; pom.xml | PASS | Jar produced at target/bankapp-java21-0.0.1-SNAPSHOT.jar; maven-compiler-plugin release=21; Boot 3.3.4 |
| Runtime/Endpoints | Health, Actuator health, OpenAPI JSON, Swagger UI, H2 console | curl checks; README; application.yml | PASS | /bank-api/health, /healthz, /actuator/health 200; /v3/api-docs 200; /swagger-ui 200; /h2-console/ 200 (trailing slash) |
| Customers CRUD | Create, read, update, delete; balances by customer | final-verification-2025-12-04.md; controllers | PASS | Delete on a linked customer returns 500 as expected; unlinked delete returns 204 |
| Accounts CRUD | Create, read, update, delete; create linked/unlinked | endpoint-verification-logs-2025-12-04.md; controllers | PASS (with latest build) | Verified success including linking to customer; older preview on 3002 returned 500 when body included bankCode/bankName; fixed by BankInfo persistence in this codebase |
| Monetary Ops | Deposit, Withdraw, Transfer; balances and transactions | final-verification-sweep-2025-12-04-3002.md; service impl | PASS | Accurate BigDecimal scaling (2 dp, HALF_UP); transactions recorded for each op |
| Data | H2 in-memory DB; H2 console; schema strategy | application.yml | PASS | jdbc:h2:mem:bankdb with DB_CLOSE_DELAY=-1; H2 console under /bank-api/h2-console/ |
| Data Seeding | Optional seed profile; idempotence | DataInitializer; README | PASS | Seeds sample customers/accounts/xrefs; transactions created if none exist; idempotent lookups |
| Security | Spring Security 6.3-compatible configuration | SecurityConfig.java | PASS | CSRF disabled; frameOptions sameOrigin; all requests permitted (dev posture) |
| OpenAPI/Swagger | springdoc v2; server URL relative to context-path; grouped API | OpenApiConfig.java; application.yml | PASS | OpenAPI servers[0].url="/bank-api"; Swagger UI at /bank-api/swagger-ui |
| Performance/Build | Toolchain and CI-ready commands | java21-ci-commands-and-setup.md | PASS | System Maven with -f recommended; wrapper OK; Temurin JDK 21 |

Cross-referenced logs:
- kavia-docs/smoke-check-2025-12-04.md
- kavia-docs/endpoint-verification-logs-2025-12-04.md
- kavia-docs/final-verification-2025-12-04.md
- kavia-docs/final-verification-sweep-2025-12-04-3002.md

## Endpoint Parity Matrix

This matrix maps endpoints between BankApp-179898 (default port 3001) and BankApp-Java21 (default port 3002). Paths are relative to the shared context-path /bank-api.

| Feature/Endpoint | BankApp-179898 (port 3001) | BankApp-Java21 (port 3002) | Parity | Notes |
| --- | --- | --- | --- | --- |
| Health (compat) | GET /healthz | GET /healthz | Achieved | 200 with {"status":"ok"} |
| Health (simple) | May not exist in source | GET /health | Achieved (superset) | 200 with {"status":"UP"} |
| Actuator | GET /actuator/health | GET /actuator/health | Achieved | 200 with {"status":"UP"} |
| OpenAPI JSON | GET /v3/api-docs | GET /v3/api-docs | Achieved | servers[0].url="/bank-api" |
| Swagger UI | GET /swagger-ui (→ /swagger-ui/index.html) | GET /swagger-ui (→ /swagger-ui/index.html) | Achieved | UI loads and Try-it-Out works |
| H2 Console | GET /h2-console/ | GET /h2-console/ | Achieved | Trailing slash required; 302 otherwise |
| Customers list | GET /customers | GET /customers | Achieved | 200; JSON array |
| Customer create | POST /customers | POST /customers | Achieved | 200; returns id |
| Customer read | GET /customers/{id} | GET /customers/{id} | Achieved | 200/404 as appropriate |
| Customer update | PUT /customers/{id} | PUT /customers/{id} | Achieved | 200; updates nested data |
| Customer delete | DELETE /customers/{id} | DELETE /customers/{id} | Achieved | 204 unlinked; fails when linked |
| Customer balances | GET /customers/{id}/balances | GET /customers/{id}/balances | Achieved | 200; list of balances |
| Accounts list | GET /accounts | GET /accounts | Achieved | 200 |
| Account create | POST /accounts[?customerId] | POST /accounts[?customerId] | Achieved (see note) | With bankCode/bankName, older preview on 3002 returned 500; fixed in current code by BankInfo persistence |
| Account read | GET /accounts/{id} | GET /accounts/{id} | Achieved | 200/404 as appropriate |
| Account update | PUT /accounts/{id} | PUT /accounts/{id} | Achieved | 200; metadata and bank info updates |
| Account delete | DELETE /accounts/{id} | DELETE /accounts/{id} | Achieved | 204 if exists |
| Balance | GET /accounts/{id}/balance | GET /accounts/{id}/balance | Achieved | 200; BigDecimal scaled to 2 dp |
| Deposit | POST /accounts/{id}/deposit | POST /accounts/{id}/deposit | Achieved | 200; transaction recorded |
| Withdraw | POST /accounts/{id}/withdraw | POST /accounts/{id}/withdraw | Achieved | 200; insufficient funds guarded |
| Transfer | POST /accounts/transfer | POST /accounts/transfer | Achieved | 200; records TRANSFER_OUT/IN |
| Transactions | GET /accounts/{id}/transactions | GET /accounts/{id}/transactions | Achieved | 200 |

## Build & Runtime Environment

The runtime targets Java 21 with Spring Boot 3.3.x. The service is designed for consistent behavior across HTTPS and proxies using a relative OpenAPI server URL and ForwardedHeaderFilter.

| Component | Setting / Version | Source |
| --- | --- | --- |
| Java | 21 (Temurin recommended) | pom.xml; logs show openjdk 21.0.9 |
| Spring Boot | 3.3.4 | pom.xml |
| Maven | 3.9.x recommended (3.8.7 used in some logs) | java21-ci-commands-and-setup.md |
| Packaging | Jar: target/bankapp-java21-0.0.1-SNAPSHOT.jar | README; mvn package |
| Port | 3002 (overridable via PORT) | application.yml |
| Address | 0.0.0.0 | application.yml |
| Context path | /bank-api | application.yml |
| Actuator | /actuator/health (exposed) | application.yml |
| Swagger UI | /swagger-ui (redirects to /swagger-ui/index.html) | application.yml |
| OpenAPI JSON | /v3/api-docs | application.yml; OpenApiConfig.java |
| H2 console | /h2-console/ | application.yml |

## Security Posture (Spring Security 6.3)

The configuration is intentionally permissive for development and verification. It disables CSRF, sets frame options to sameOrigin for the H2 console, and permits all requests, including docs and health endpoints. This mirrors the original environment’s developer-friendly settings while using Spring Security 6.3-compatible APIs.

| Setting | Java 21 App Behavior | Source |
| --- | --- | --- |
| Authentication | No authentication enforced; all requests permitted | SecurityConfig.java |
| CSRF | Disabled | SecurityConfig.java |
| Frame Options | sameOrigin (H2 console support) | SecurityConfig.java |
| Swagger/OpenAPI | Explicitly permitted; UI and docs accessible | SecurityConfig.java |
| Actuator health | Permitted | SecurityConfig.java |
| Production Note | Harden policies for prod: restrict routes, enable proper authZ/authN | Report recommendation |

## Database & Data Seeding

The application uses an in-memory H2 database for development and test. A seed profile is available to pre-load customers, accounts, xrefs, and initial deposit transactions in an idempotent way.

- Database: jdbc:h2:mem:bankdb; DB_CLOSE_DELAY=-1; DB_CLOSE_ON_EXIT=FALSE
- H2 console: /bank-api/h2-console/ (trailing slash)
- JPA: hibernate.ddl-auto=update; H2 dialect configured

Seeding profile (“seed”):
- Creates Alice and Bob customers with address/contact details if absent.
- Creates accounts with initial balances when accounts are missing.
- Links customers to accounts (PRIMARY role).
- Adds initial deposit transactions only when none exist.

Important note: To avoid “unsaved transient entity” issues for BankInfo (ManyToOne without cascade), DataInitializer does not persist BankInfo. Application flows in BankingServiceImpl persist/reuse BankInfo by bankCode when provided.

| Aspect | Behavior | Source |
| --- | --- | --- |
| DB | In-memory H2; console enabled; CORS allowed | application.yml |
| DDL | update | application.yml |
| Seed profile | idempotent seeding of customers/accounts/xrefs/transactions | DataInitializer.java |
| BankInfo in seed | Not persisted by seeder to avoid transient entity errors | DataInitializer.java |
| BankInfo in service | Persist/reuse by bankCode on create/update when bankCode is non-blank | BankingServiceImpl.java |

## OpenAPI/Swagger Coverage

OpenAPI is provided via springdoc-openapi v2 with a relative server URL equal to the servlet context path. This makes the UI and Try-it-Out robust across HTTPS and reverse proxies. A GroupedOpenApi bean exposes all routes.

- OpenAPI JSON: /bank-api/v3/api-docs
- Swagger UI: /bank-api/swagger-ui
- servers[0].url: "/bank-api" (relative)
- ForwardedHeaderFilter registered and server.forward-headers-strategy=framework enabled

| Element | Detail | Source |
| --- | --- | --- |
| OpenAPI Info | Title “BankApp Java21 API”; version from package, fallback “v1” | OpenApiConfig.java |
| Server URL | Relative to context-path | OpenApiConfig.java |
| Group | group “bankapp”; paths /** | OpenApiConfig.java |
| Annotations | @Operation used in controllers | Controllers |
| HTTPS safety | Relative URL + ForwardedHeaderFilter | OpenApiConfig.java; application.yml |

## Known Differences, Notes, and Mitigations

The migration targets parity, with a few operational notes observed during verification:

- Account creation with bankCode/bankName returned HTTP 500 on an older 3002 preview instance. Root cause: unsaved BankInfo (ManyToOne without cascade). Mitigation: Current code in BankingServiceImpl persists or reuses BankInfo by bankCode before associating the Account; verified PASS when running the latest build (e.g., on port 3003). Action: redeploy current build to 3002.
- Customer deletion fails (500) when the customer is still linked to accounts. This mirrors expected behavior in many CRUD flows to prevent orphan references. Deleting unlinked customers returned 204.
- H2 console requires the trailing slash (/h2-console/). Without it, the endpoint responds with a 302 redirect. Use curl -L or access with the trailing slash.
- Health endpoints: The Java 21 app provides both /healthz and /health (the latter may be a superset compared to the source). Both coexist for compatibility.
- CORS behavior is configured for preview and typical localhost origins. Update ALLOWED_ORIGINS to match your environment.

| Topic | Observed Difference | Mitigation/Status |
| --- | --- | --- |
| Account create with bank info | 500 on older preview (3002) | Fixed in code; redeploy the latest build |
| Customer delete when linked | 500 | Expected constraint; unlink accounts before delete |
| H2 console URL | 302 without trailing slash | Use /h2-console/ or curl -L |
| Health endpoints | /health added in Java21 | Backwards-compatible; /healthz still available |

## Modernization Improvements Performed

The Java 21 migration introduced several modernization improvements aligned with Spring Boot 3 conventions and operational safety behind proxies:

- Java 21 toolchain and bytecode (maven-compiler-plugin release=21) for the BankApp-Java21 service.
- Spring Boot 3.3.4 parent with compatible starters for web, actuator, validation, data-jpa, and security.
- Springdoc OpenAPI v2 with a relative server URL that equals the servlet context path, ensuring HTTPS and reverse proxy compatibility.
- ForwardedHeaderFilter and server.forward-headers-strategy=framework to honor X-Forwarded-* headers.
- Global CORS configuration (code + YAML) driven by ALLOWED_ORIGINS.
- Spring Security 6.3-compatible configuration using the current HttpSecurity DSL, CSRF disabled for development, and sameOrigin frame options for H2.
- Optional seed profile with idempotent data initialization and defensive handling for BankInfo.
- Monetary operations with consistent BigDecimal scaling to 2 decimal places (HALF_UP) and transaction recording for auditability.

### Recommended Next Steps

- Harden security for non-development deployments: restrict routes, enable authentication and authorization, and consider rate limiting.
- Add automated tests (unit and integration) covering CRUD, monetary operations, and security rules; integrate with CI.
- Introduce database migration tooling (e.g., Flyway) for production schemas.
- Expand observability with more actuator endpoints, structured logging, and tracing if needed.
- Confirm cascade rules or service-level handling for BankInfo and other relationships to ensure consistent persistence semantics.
- Establish performance baselines and consider caching where appropriate.

## References

- README: runtime configuration and curl examples.
- Verification logs:
  - kavia-docs/smoke-check-2025-12-04.md
  - kavia-docs/endpoint-verification-logs-2025-12-04.md
  - kavia-docs/final-verification-2025-12-04.md
  - kavia-docs/final-verification-sweep-2025-12-04-3002.md
- CI and Commands: kavia-docs/java21-ci-commands-and-setup.md
- Migration guide: kavia-docs/java21-migration-modernization-guide.md
- Configuration and code: application.yml, SecurityConfig.java, OpenApiConfig.java, WebCorsConfig.java, DataInitializer.java, controllers and service implementations.

# Migration and Modernization Guide: Upgrade BankApp-179898 (Java 17) to Java 21 in Migration-Java-21-43251

## Overview and scope

This document provides a detailed, end-to-end guide to migrate and modernize the Spring Boot banking application from Java 17 to Java 21, targeting the new workspace:
- Source (current): /home/kavia/workspace/code-generation/BankApp-179898
- Target (Java 21): /home/kavia/workspace/code-generation/Migration-Java-21-43251

The guide covers prerequisites, JDK and Maven toolchains, Spring Boot and dependency alignment, Jakarta vs javax migration considerations (Boot 3), OpenAPI with HTTPS and a fixed context-path (/bank-api), Actuator and health endpoints, Security configuration, H2 database, global CORS, build and packaging commands (using system Maven with -f), CI setup for GitHub Actions and GitLab CI, verification checklists, and rollback steps.

Key differences to expect after migration:
- Java 21 toolchain and bytecode level
- Spring Boot 3.3.x parent in the new workspace
- Jakarta namespace throughout the Spring stack (Boot 3 uses jakarta.*)
- springdoc-openapi v2 (not Springfox)
- Consistent context-path /bank-api and HTTPS-friendly OpenAPI server configuration
- Global CORS and management endpoint exposure aligned to modern defaults

## 1. Prerequisites

- Java Development Kit (JDK) 21 installed (Temurin recommended).
- Maven 3.9.x installed on the system (you will use system Maven and -f to avoid wrapper-related issues).
- A shell environment where you can set JAVA_HOME (Linux/macOS or Windows).
- Network access to Maven Central for dependencies.

Quick checks:
```bash
java -version
mvn -version
```

For a strict Java 21 build, ensure that either:
- Maven itself runs on Java 21 (preferred), or
- a Java 21 toolchain is configured and activated (see Section 3).

## 2. Spring Boot and dependency alignment

The target workspace uses Spring Boot 3.3.x and Java 21. Align the application at the target with:
- Parent POM: org.springframework.boot:spring-boot-starter-parent:3.3.4
- Java 21 release level
- springdoc-openapi-starter-webmvc-ui v2.x (Jakarta/Spring Boot 3 compatible)
- Optional JAXB runtime for Java 21 if your code relies on JAXB (jakarta.xml.bind-api + org.glassfish.jaxb:jaxb-runtime)
- Add H2 database dependency at runtime scope if the domain requires it

Example parent and key deps (already present in Migration-Java-21-43251):
```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.3.4</version>
  <relativePath/>
</parent>

<properties>
  <java.version>21</java.version>
  <maven.compiler.release>21</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

If using JAXB at runtime on Java 21:
```xml
<dependency>
  <groupId>jakarta.xml.bind</groupId>
  <artifactId>jakarta.xml.bind-api</artifactId>
  <version>4.0.x</version>
</dependency>
<dependency>
  <groupId>org.glassfish.jaxb</groupId>
  <artifactId>jaxb-runtime</artifactId>
  <version>4.0.x</version>
</dependency>
```

## 3. Maven compiler and Toolchains for Java 21

The target workspace is set to compile with Java 21 via the maven-compiler-plugin:
```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.13.0</version>
  <configuration>
    <release>${maven.compiler.release}</release> <!-- 21 -->
  </configuration>
</plugin>
```

Two operational strategies exist:

### 3.1 Preferred: Run Maven itself on JDK 21
Set JAVA_HOME to your JDK 21 and run mvn:
```bash
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
```
You should see a Java 21 runtime. This avoids toolchains complexity.

### 3.2 Alternative: Use Maven Toolchains (when Maven runs on JDK 17)
If your environment starts Maven on JDK 17 but you want to compile with JDK 21, configure a toolchain:

User-level toolchain (recommended):
- File: ~/.m2/toolchains.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 https://maven.apache.org/xsd/toolchains-1.1.0.xsd">
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
      <vendor>any</vendor>
    </provides>
    <configuration>
      <jdkHome>/absolute/path/to/your/jdk-21</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

Optionally add the maven-toolchains-plugin in the target POM to ensure the toolchain is used during build:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-toolchains-plugin</artifactId>
      <version>3.0.0</version>
      <executions>
        <execution>
          <goals>
            <goal>toolchain</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <toolchains>
          <jdk>
            <version>21</version>
            <vendor>any</vendor>
          </jdk>
        </toolchains>
      </configuration>
    </plugin>
  </plugins>
</build>
```

With the toolchain in place, Maven may run on JDK 17 while the compiler uses JDK 21. If no toolchain is present and your Maven runs on JDK < 21, a strict Java 21 build will fail by design.

## 4. Jakarta vs javax considerations (Boot 3 confirmation)

Spring Boot 3+ migrated the Java EE APIs to Jakarta EE. This means:
- javax.persistence.* becomes jakarta.persistence.*
- javax.validation.* becomes jakarta.validation.*
- Servlet APIs move from javax.servlet.* to jakarta.servlet.*

The source project (BankApp-179898) already uses Boot 3 and the Jakarta ecosystem. If you copy or refactor code into the new Java 21 workspace, verify imports remain in jakarta.* packages and remove any legacy Springfox references (replaced by springdoc-openapi starter).

Checklist for code:
- All JPA annotations and imports use jakarta.persistence.*
- Validation uses jakarta.validation.*
- Remove old Springfox dependencies/config and replace with springdoc-openapi v2 starter
- Ensure @OpenAPIDefinition or equivalent springdoc configuration is retained

## 5. OpenAPI (springdoc) configuration with HTTPS and context path

The target workspace sets a relative OpenAPI Server URL based on the servlet context-path and registers a ForwardedHeaderFilter. This combination ensures correct behavior behind proxies and under HTTPS without hardcoding scheme/host.

- Context-path: /bank-api
- application.yml:
```yaml
server:
  port: ${PORT:3002}
  address: 0.0.0.0
  forward-headers-strategy: framework
  servlet:
    context-path: /bank-api

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui
```

- OpenApiConfig (target):
```java
@Bean
public ForwardedHeaderFilter forwardedHeaderFilter() {
  return new ForwardedHeaderFilter();
}

@Bean
public OpenAPI applicationOpenAPI(@Value("${server.servlet.context-path:}") String contextPath) {
  String basePath = (contextPath == null || contextPath.isBlank()) ? "/" : contextPath;
  if (!basePath.startsWith("/")) basePath = "/" + basePath;
  if (basePath.length() > 1 && basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length() - 1);
  OpenAPI openAPI = new OpenAPI();
  openAPI.setServers(List.of(new io.swagger.v3.oas.models.servers.Server().url(basePath)));
  return openAPI;
}
```

Why this matters:
- A relative server URL avoids mixed-content issues and respects HTTPS termination at proxies.
- ForwardedHeaderFilter + server.forward-headers-strategy=framework allows Spring to honor X-Forwarded-* headers and render correct absolute links when needed.

Swagger UI paths after start:
- UI: http(s)://host:3002/bank-api/swagger-ui (redirects to /bank-api/swagger-ui/index.html)
- OpenAPI JSON: http(s)://host:3002/bank-api/v3/api-docs

## 6. Actuator and health endpoints

Actuator is enabled in the target workspace (starter included) with health/info exposure:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
      cors:
        allowed-origins: ${ALLOWED_ORIGINS:https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3000,https://vscode-internal-15158-beta.beta01.cloud.kavia.ai:3002,http://localhost:3000,http://localhost:4000}
        allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
        allowed-headers: Content-Type,Authorization,X-Requested-With,Origin,Accept
        exposed-headers: Location,Link
        allow-credentials: true
        max-age: 3600
  endpoint:
    health:
      show-details: "never"
```

Endpoints to verify:
- /bank-api/actuator/health (should return 200 and status UP)
- A simple REST health endpoint is also present in the target workspace:
  - /bank-api/health returns {"status":"UP"}
- In the source workspace, a similar health endpoint exists under /bank-api/healthz returning {"status":"ok"}

Both health endpoints are acceptable; standardizing to /health or /healthz is a product decision. Keep both during transition if helpful.

## 7. Security adjustments (Spring Security 6, Boot 3)

The source project currently disables authentication globally to simplify testing:
- All requests are permitted
- CSRF and frame options are disabled for ease of development (not for production)

To replicate this behavior in the Java 21 workspace, configure Security as follows (example from the source workspace):

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
      .httpBasic(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
```

Notes:
- The above is intentionally permissive for development. For production, consider explicitly permitting only:
  - /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html
  - /h2-console/**
  - /actuator/health
  - application-necessary GET endpoints for landing pages
  and require authentication/authorization elsewhere.

## 8. H2 configuration

If your service uses H2 for development and tests, add H2 to the target POM (runtime scope recommended) and enable the console.

POM dependency:
```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```

application.yml:
```yaml
spring:
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
# Datasource defaults to jdbc:h2:mem:testdb under Boot 3 if not overridden.
# To customize:
#  datasource:
#    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
#    driverClassName: org.h2.Driver
#    username: sa
#    password:
```

The H2 console is reachable at:
- /bank-api/h2-console/

When security is permissive (as above), the console should render without additional configuration.

## 9. Global CORS

The target workspace configures global CORS both at MVC and at management endpoints. Two complementary approaches are used:

- Java-based WebMvcConfigurer:
```java
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {
  public WebCorsConfig(@Value("${ALLOWED_ORIGINS:...}") String allowedOriginsProp) { ... }
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
      .allowedOrigins(this.allowedOrigins)
      .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
      .allowedHeaders("Content-Type","Authorization","X-Requested-With","Origin","Accept")
      .exposedHeaders("Location","Link")
      .allowCredentials(true)
      .maxAge(3600);
  }
}
```

- application.yml for both MVC and Actuator:
```yaml
spring:
  web:
    cors:
      allowed-origins: ${ALLOWED_ORIGINS:...}
      allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
      allowed-headers: Content-Type,Authorization,X-Requested-With,Origin,Accept
      exposed-headers: Location,Link
      allow-credentials: true
      max-age: 3600

management:
  endpoints:
    web:
      cors:
        allowed-origins: ${ALLOWED_ORIGINS:...}
        allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
        allowed-headers: Content-Type,Authorization,X-Requested-With,Origin,Accept
        exposed-headers: Location,Link
        allow-credentials: true
        max-age: 3600
```

Use the ALLOWED_ORIGINS environment variable to set a comma-separated list of allowed origins for previews or local development:
```bash
export ALLOWED_ORIGINS="https://your-preview-host:3000,http://localhost:3000"
```

## 10. Build, package, and run commands (system Maven with -f)

To avoid mvnw/wrapper execution issues in CI or constrained environments, use system Maven and the -f flag to point to the target POM.

Build (skip tests):
```bash
mvn -q -DskipTests -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml clean package
```

Build (with tests):
```bash
mvn -q -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml clean verify
```

Run via Spring Boot plugin (respecting context-path /bank-api and default port 3002):
```bash
mvn -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=3002 --server.address=0.0.0.0"
```

Package and run the jar directly:
```bash
mvn -q -DskipTests -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml clean package
java -jar /home/kavia/workspace/code-generation/Migration-Java-21-43251/target/bankapp-java21-0.0.1-SNAPSHOT.jar
```

Tip: Always quote the entire value passed to -Dspring-boot.run.arguments to avoid Maven parsing issues.

## 11. CI setup

### 11.1 GitHub Actions

Example workflow .github/workflows/build-java21.yml:

```yaml
name: Build (Java 21)

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Temurin JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Build (skip tests)
        run: mvn -q -DskipTests -f Migration-Java-21-43251/pom.xml clean package

      - name: Verify (tests)
        run: mvn -q -f Migration-Java-21-43251/pom.xml test

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: bankapp-java21-artifacts
          path: Migration-Java-21-43251/target/*.jar
```

### 11.2 GitLab CI

Example .gitlab-ci.yml:

```yaml
stages:
  - build
  - test

variables:
  MAVEN_CLI_OPTS: "-q -B"
  MAVEN_OPTS: "-DskipTests"

build:
  stage: build
  image: eclipse-temurin:21
  script:
    - apt-get update && apt-get install -y maven
    - mvn $MAVEN_CLI_OPTS -f Migration-Java-21-43251/pom.xml clean package -DskipTests
  artifacts:
    paths:
      - Migration-Java-21-43251/target/*.jar
    expire_in: 1 week

test:
  stage: test
  image: eclipse-temurin:21
  script:
    - apt-get update && apt-get install -y maven
    - mvn $MAVEN_CLI_OPTS -f Migration-Java-21-43251/pom.xml test
```

Notes:
- Using system Maven with -f avoids mvnw permission issues.
- Use a JDK 21 base image or install JDK 21 during the job.
- Enable Maven cache if your environment supports it to speed up builds.

## 12. Step-by-step migration

1. Prepare the environment:
   - Install JDK 21; set JAVA_HOME accordingly.
   - Confirm java -version reports 21.x.
   - Confirm mvn -version sees Java 21 (or set up toolchains if Maven must run on 17).

2. Align the POM in the target workspace:
   - spring-boot-starter-parent 3.3.4.
   - maven-compiler-plugin 3.13.0 with <release>21</release>.
   - springdoc-openapi-starter-webmvc-ui 2.x.
   - Add H2 dependency if you need the in-memory database for dev/test.

3. Copy/port application code from BankApp-179898 to the target:
   - Ensure all imports are jakarta.* for JPA/Validation/Servlet.
   - Replace any Springfox/OpenAPI legacy config with springdoc v2.
   - Maintain the servlet context-path /bank-api.

4. Configure OpenAPI and Forwarded headers:
   - Keep ForwardedHeaderFilter bean.
   - Use a relative OpenAPI server URL set to the context-path.

5. Security configuration:
   - For development, you can permit all requests and disable CSRF and frame options (as in the source).
   - For production, restrict to least-privilege access and permit only docs, health, and necessary endpoints.

6. Enable H2 console (if needed) and JPA settings:
   - Enable spring.h2.console.enabled=true.
   - Use Boot 3 defaults for the in-memory datasource or configure explicitly.

7. Global CORS:
   - Keep WebCorsConfig and/or YAML-based CORS entries.
   - Set ALLOWED_ORIGINS for your preview or local dev hosts.

8. Build and run:
   - mvn -q -DskipTests -f Migration-Java-21-43251/pom.xml clean package
   - mvn -f Migration-Java-21-43251/pom.xml spring-boot:run -Dspring-boot.run.arguments="--server.port=3002 --server.address=0.0.0.0"

9. Verify endpoints:
   - GET /bank-api/health
   - GET /bank-api/actuator/health
   - GET /bank-api/v3/api-docs
   - GET /bank-api/swagger-ui
   - GET /bank-api/h2-console (if enabled)

10. Wire up CI (GitHub Actions or GitLab CI) with Java 21 and system Maven (-f).

## 13. Verification checklist

- Runtime and endpoints:
  - Application starts on port 3002 with context-path /bank-api.
  - /bank-api/health returns 200 with {"status":"UP"}.
  - /bank-api/actuator/health returns 200 with status UP.
  - /bank-api/v3/api-docs returns 200 with an "openapi" field.
  - /bank-api/swagger-ui resolves to /bank-api/swagger-ui/index.html and loads the UI.
  - /bank-api/h2-console opens the H2 web console page (if enabled).

- OpenAPI and HTTPS:
  - No mixed-content warnings when accessed via HTTPS.
  - Try-it-Out calls from Swagger UI succeed against /bank-api routes.

- CORS:
  - Browser-based tools (Swagger UI or frontends) can call APIs without CORS failures.
  - Management endpoints CORS behaves consistently with app endpoints.

- Security:
  - Intended endpoints are accessible without auth in development (or properly guarded in production).
  - Frame options disabled only if H2 console is in use.

- Build:
  - System Maven builds succeed on JDK 21.
  - CI passes with JDK 21 images and -f command usage.

## 14. Rollback plan

If you must revert to the Java 17 baseline quickly:

- Keep Boot 3.x (Boot 3.3 supports Java 17), but revert the compiler release level to 17, or simply run the source workspace (BankApp-179898) as-is:
  - In the POM, change <maven.compiler.release> to 17.
  - Ensure Maven runs on JDK 17 (or use toolchains for 17).
  - Retain springdoc-openapi v2 and Jakarta imports.

- Alternative: revert to the original source repository state:
  - Revert migration commits and continue to build BankApp-179898 with its existing configuration.
  - Source runtime defaults:
    - Port: 3001
    - Context path: /bank-api
  - Use the provided scripts (e.g., ./mvnw or ./start.sh) if your environment allows wrappers.

- Risk mitigations:
  - Keep a tag before migration work (e.g., bankapp-pre-java21-migration).
  - Ensure CI pipelines for both Java 17 and Java 21 paths exist during the transition until production is switched.

## 15. Final checklist

- JDK 21 installed and detected by Maven or toolchains configured for 21.
- POM aligned to Boot 3.3.x with maven-compiler-plugin release 21.
- springdoc-openapi v2 starter present; no legacy Springfox.
- Jakarta imports verified across all domain and web layers.
- OpenAPI configured with a relative server URL and ForwardedHeaderFilter.
- Global CORS enabled for app and management endpoints; ALLOWED_ORIGINS set appropriately.
- H2 dependencies and console (if required) enabled.
- Build/run commands using system Maven with -f POM documented and working.
- CI pipelines updated to run on Java 21 and build with -f.
- Health, OpenAPI JSON, Swagger UI, and (optionally) H2 console verified.
- Rollback instructions documented and a pre-migration tag created.

## Appendix: Paths and port summary

- Target workspace: Migration-Java-21-43251
  - Default port: 3002
  - Context path: /bank-api
  - Example endpoints:
    - /bank-api/health
    - /bank-api/actuator/health
    - /bank-api/v3/api-docs
    - /bank-api/swagger-ui
    - /bank-api/h2-console (if enabled)
- Source workspace: BankApp-179898
  - Default port: 3001
  - Context path: /bank-api
  - Example endpoints:
    - /bank-api/healthz
    - /bank-api/actuator/health
    - /bank-api/v3/api-docs
    - /bank-api/swagger-ui
    - /bank-api/h2-console

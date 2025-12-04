# Java 21 CI Commands and Setup for Migration-Java-21-43251

## Why your last command failed
You executed `/mvnw -f ...`, which points to the filesystem root and not to this project’s wrapper. Because CI and preview systems often run commands from a different working directory, Maven fails with the message similar to: `-Dmaven.multiModuleProjectDirectory system property is not set.` Use the absolute path to the wrapper and pass `-f` with the absolute `pom.xml` to avoid CWD issues.

Correct wrapper path for this workspace:
- /home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw

## Exact local commands (Java 21, Maven Wrapper)
Ensure the wrapper is executable, then use absolute paths and the `-f` flag targeting the absolute POM path. The application is configured for port 3002 and context-path `/bank-api`.

```bash
# 1) Make wrapper executable (once per environment)
chmod +x /home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw

# 2) Build (skip tests)
# Java 21 is required; Maven Compiler is configured with release=21 in pom.xml
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  -q -DskipTests clean package

# 3) Install (skip tests)
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  -q -DskipTests install

# 4) Run tests
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  -q test

# 5) Run the application (default port 3002; address 0.0.0.0; context-path /bank-api)
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=${PORT:-3002} --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"
```

## Manifest-safe commands (absolute wrapper + fallback)
These commands are safe for manifests or platforms that may strip the executable bit. The `|| sh ...` fallback runs the wrapper via `sh` if direct execution is blocked.

```bash
# buildCommand
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  -q -DskipTests clean package \
  || sh /home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
       -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
       -q -DskipTests clean package

# installCommand
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  -q -DskipTests install \
  || sh /home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
       -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
       -q -DskipTests install

# testCommand
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  -q test \
  || sh /home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
       -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
       -q test

# startCommand
/home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
  -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
  spring-boot:run \
  -Dspring-boot.run.arguments="--server.port=${PORT:-3002} --server.address=0.0.0.0 --server.servlet.context-path=/bank-api" \
  || sh /home/kavia/workspace/code-generation/Migration-Java-21-43251/mvnw \
       -f /home/kavia/workspace/code-generation/Migration-Java-21-43251/pom.xml \
       spring-boot:run \
       -Dspring-boot.run.arguments="--server.port=${PORT:-3002} --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"
```

## Sample CI: GitHub Actions (Java 21)
This workflow installs Temurin JDK 21 and uses absolute paths via `$GITHUB_WORKSPACE` to avoid CWD issues and the `-Dmaven.multiModuleProjectDirectory` error.

```yaml
name: CI (Java 21)
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Build (skip tests)
        run: |
          chmod +x "$GITHUB_WORKSPACE/mvnw"
          "$GITHUB_WORKSPACE/mvnw" \
            -f "$GITHUB_WORKSPACE/pom.xml" \
            -q -DskipTests clean package

      - name: Test
        run: |
          "$GITHUB_WORKSPACE/mvnw" \
            -f "$GITHUB_WORKSPACE/pom.xml" \
            -q test

      # Optional preview run (for ephemeral environments)
      - name: Run (optional)
        run: |
          "$GITHUB_WORKSPACE/mvnw" \
            -f "$GITHUB_WORKSPACE/pom.xml" \
            spring-boot:run \
            -Dspring-boot.run.arguments="--server.port=3002 --server.address=0.0.0.0 --server.servlet.context-path=/bank-api"
```

## Sample CI: GitLab CI (Java 21)
This pipeline uses an Eclipse Temurin JDK 21 image and invokes the wrapper with absolute paths via `$CI_PROJECT_DIR`.

```yaml
image: eclipse-temurin:21

stages: [build, test]

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

cache:
  paths:
    - .m2/repository

build:
  stage: build
  script:
    - chmod +x "$CI_PROJECT_DIR/mvnw"
    - "$CI_PROJECT_DIR/mvnw" \
        -f "$CI_PROJECT_DIR/pom.xml" \
        -q -DskipTests clean package

test:
  stage: test
  script:
    - "$CI_PROJECT_DIR/mvnw" \
        -f "$CI_PROJECT_DIR/pom.xml" \
        -q test
```

## Health check verification
After the app starts (default: port 3002; context-path `/bank-api`), verify health endpoints:

```bash
# Actuator health (UP expected)
curl -i http://localhost:3002/bank-api/actuator/health

# Simple REST health from HealthController ({"status":"UP"} expected)
curl -i http://localhost:3002/bank-api/health
```

If you later add OpenAPI support, verify:
```bash
# Only when springdoc-openapi is added to this project
curl -i http://localhost:3002/bank-api/v3/api-docs
curl -i http://localhost:3002/bank-api/swagger-ui/index.html
```

### Notes
- The `pom.xml` sets Java 21 (maven-compiler-plugin with `<release>21</release>`), so a JDK 21 runtime is required.
- Using the absolute wrapper path together with `-f /absolute/pom.xml` reliably avoids the multi-module directory error in CI and manifest-driven platforms.

# project-build Specification

## Purpose

Defines how the Amusement Park application is structured, compiled, tested and packaged as an executable Maven project on JDK 21.

## Requirements

### Requirement: Maven quickstart project on JDK 21
The project SHALL be a Maven project generated from `org.apache.maven.archetypes:maven-archetype-quickstart`, retaining the archetype's standard layout (`pom.xml`, `src/main/java`, `src/test/java`) and compiling with Java release 21.

#### Scenario: Project compiles with JDK 21
- **WHEN** a developer runs `mvn compile` with a JDK 21 or newer toolchain
- **THEN** the build succeeds and the compiled classes target Java release 21

#### Scenario: Standard archetype layout is retained
- **WHEN** the repository is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist at the project root

### Requirement: Test suite runs with mvn test
The project SHALL include the basic testing structure and `mvn test` SHALL compile the project and pass all tests.

#### Scenario: Tests pass
- **WHEN** a developer runs `mvn test`
- **THEN** the build reports success with zero test failures or errors

### Requirement: Executable JAR
Running `mvn package` SHALL produce `target/parque.jar`, whose manifest declares the application's main class and which bundles the runtime dependencies, so that it can be started with `java -jar` alone.

#### Scenario: JAR runs directly
- **WHEN** a developer runs `mvn package` and then `java -jar target/parque.jar`
- **THEN** the application starts using the configured main class without requiring a classpath argument, and password hashing works

### Requirement: Standard library only
The application code SHALL use only the Java standard library at runtime plus a single BCrypt library declared in `pom.xml` for password hashing; no frameworks, databases or other runtime dependencies SHALL be introduced.

#### Scenario: No runtime dependencies
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** apart from test-scoped dependencies, only the BCrypt library is declared

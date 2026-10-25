# project-build Specification

## Purpose

Defines how the Amusement Park application is built, tested and packaged with Maven on Java 21.

## Requirements

### Requirement: Maven quickstart project on Java 21
The project SHALL be a Maven project derived from `org.apache.maven.archetypes:maven-archetype-quickstart`, keeping its standard layout (`pom.xml`, `src/main/java`, `src/test/java`) and compiling for Java 21.

#### Scenario: Project compiles for Java 21
- **WHEN** `mvn compile` is run on a JDK 21 or newer
- **THEN** the build succeeds producing Java 21 bytecode

#### Scenario: Standard layout retained
- **WHEN** the repository is inspected
- **THEN** `pom.xml`, `src/main/java` and `src/test/java` exist at the project root

### Requirement: Tests run with mvn test
The project SHALL include a test structure and `mvn test` SHALL compile the project and pass all tests.

#### Scenario: Test suite passes
- **WHEN** `mvn test` is run
- **THEN** the build succeeds and reports zero test failures

### Requirement: Executable JAR
`mvn package` SHALL produce a JAR whose manifest declares the application main class so it can be launched with `java -jar`.

#### Scenario: Run packaged JAR
- **WHEN** `mvn package` is run and then `java -jar` is executed on the produced JAR
- **THEN** the application starts without needing a classpath or main-class argument

### Requirement: Standard library only
The production code SHALL use only the Java standard library; no external runtime dependencies, frameworks or databases SHALL be declared.

#### Scenario: No runtime dependencies
- **WHEN** the `pom.xml` dependencies are inspected
- **THEN** only test-scoped dependencies are declared

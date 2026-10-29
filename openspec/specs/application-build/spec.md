# application-build Specification

## Purpose

Defines how the Amusement Park application is structured, built with Maven on JDK 21 and packaged as an executable JAR that runs outside any IDE.

## Requirements

### Requirement: Maven project targeting JDK 21
The project SHALL be a Maven project with the standard apache-maven-quickstart directory structure (`pom.xml`, `src/main/java`, `src/test/java`) configured to compile for Java 21.

#### Scenario: Project compiles with Maven
- **WHEN** `mvn package` is run from the repository root with JDK 21
- **THEN** the build succeeds and compiles the sources at Java release 21

### Requirement: Executable JAR
`mvn package` SHALL produce an executable JAR in `target/` whose manifest declares the application entry point, so that it starts with `java -jar` without an IDE or extra classpath configuration.

#### Scenario: Start the JAR without arguments
- **WHEN** `java -jar target/amusement-park.jar` is run
- **THEN** the application starts in interactive mode and waits for commands on standard input

#### Scenario: Start the JAR with a command file
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** the application processes the commands contained in `input.txt`

### Requirement: Standard library only
The application SHALL use only the Java standard library at runtime, except for the BCrypt library used to protect passwords; it MUST NOT use databases, frameworks, other external libraries, regular expressions or encryption mechanisms other than BCrypt password hashing. The executable JAR MUST include the BCrypt library so it runs without extra configuration.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** no dependency other than `org.mindrot:jbcrypt` and test-scoped testing libraries is declared

#### Scenario: Self-contained JAR
- **WHEN** `java -jar target/amusement-park.jar` creates a user
- **THEN** the password is hashed without a ClassNotFoundException

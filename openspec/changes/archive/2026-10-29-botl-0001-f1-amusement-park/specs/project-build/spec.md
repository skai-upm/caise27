# Spec Delta

## Purpose

Defines how the Amusement Park application is structured, built with Maven on JDK 21, and packaged as an executable JAR that runs outside any IDE.

## ADDED Requirements

### Requirement: Standard Maven project on JDK 21
The project SHALL be a Maven project following the standard apache-maven-quickstart directory layout (`pom.xml`, `src/main/java`, `src/test/java`) and SHALL be configured to compile for Java 21.

#### Scenario: Project compiles with Maven
- **WHEN** `mvn package` is run on a clean checkout with JDK 21
- **THEN** the build completes successfully

#### Scenario: Java release level is 21
- **WHEN** the `pom.xml` compiler configuration is inspected
- **THEN** the compiler release (or source/target) level is 21

### Requirement: Executable JAR produced by mvn package
`mvn package` SHALL produce `target/nombre-aplicacion.jar` whose manifest declares the application entry-point class, so that it can be started with `java -jar target/nombre-aplicacion.jar`.

#### Scenario: JAR starts with java -jar
- **WHEN** the user runs `java -jar target/nombre-aplicacion.jar` after `mvn package`
- **THEN** the application starts in interactive mode and waits for commands from standard input

#### Scenario: JAR accepts a command file argument
- **WHEN** the user runs `java -jar target/nombre-aplicacion.jar input.txt`
- **THEN** the application processes the commands contained in `input.txt`

### Requirement: Standard library only at runtime
The application SHALL use only the Java standard library at runtime: no databases, frameworks, external libraries, regular expressions or encryption mechanisms.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** no dependency has compile or runtime scope (test-scoped testing libraries only)

#### Scenario: No regular expressions in source
- **WHEN** the application source code is inspected
- **THEN** it does not use `java.util.regex`, `String.matches`, `String.replaceAll`, `String.replaceFirst`, or regex-based `split` patterns other than the literal `;`, `@` and `.` separators

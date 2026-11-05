# Spec Delta

## Purpose

Defines how the Amusement Park application is packaged and launched as a standalone executable JAR, which runtime technical restrictions it honors, and how it behaves in a controlled way when input cannot be processed.

## ADDED Requirements

### Requirement: Executable JAR produced by Maven
Running `mvn package` on a clean checkout SHALL produce an executable JAR at `target/amusement-park.jar` that starts the application with `java -jar`, with no IDE, source change or extra configuration.

#### Scenario: Package and run interactively
- **GIVEN** a clean checkout with JDK 21 and Maven
- **WHEN** `mvn package` is run and then `java -jar target/amusement-park.jar` is started with `HELP` and `EXIT` on standard input
- **THEN** the help text is printed and the process ends normally with exit status 0

#### Scenario: Run with a command file
- **GIVEN** the packaged JAR and a file `input.txt` containing `HELP` then `EXIT`
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** the commands of the file are processed in order and the process ends normally

#### Scenario: Nonexistent command file
- **GIVEN** the packaged JAR
- **WHEN** it is started with the path of a file that does not exist
- **THEN** an error message is printed, no stack trace is shown, and the process ends normally

### Requirement: Controlled handling of failing commands
If executing a command fails unexpectedly, the application SHALL report an error message for that line and continue processing the following lines; the session SHALL NOT terminate abnormally.

#### Scenario: Failure inside one command
- **GIVEN** a command whose execution raises an unexpected error, followed by `HELP`
- **WHEN** the input is processed
- **THEN** an error message is shown for the failing line
- **AND** `HELP` is still executed

### Requirement: Delivery 1 technical restrictions
The application SHALL target Java 21, SHALL use only the Java standard library at runtime (no databases, frameworks or external runtime libraries), and its production code SHALL NOT use regular expressions or encryption mechanisms; command parsing SHALL rely on basic String operations.

#### Scenario: No runtime dependencies
- **GIVEN** the project `pom.xml`
- **WHEN** the declared dependencies are inspected
- **THEN** every dependency has `test` scope

#### Scenario: No regular expressions or encryption in production code
- **GIVEN** the production sources under `src/main/java`
- **WHEN** they are inspected
- **THEN** they do not import `java.util.regex`, `javax.crypto` or `java.security`, nor call `matches`, `replaceAll`, `replaceFirst` or `Pattern`

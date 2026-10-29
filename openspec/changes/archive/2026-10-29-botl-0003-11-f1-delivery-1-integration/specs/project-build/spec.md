# Spec Delta

## ADDED Requirements

### Requirement: Executable JAR
Building the project with `mvn package` SHALL produce an executable JAR in `target/` whose manifest declares the application entry point, so the application starts with `java -jar` without an IDE, source changes or additional configuration.

#### Scenario: Interactive execution of the JAR
- **WHEN** `java -jar target/amusement-park.jar` is run without arguments and `HELP` then `EXIT` are typed on standard input
- **THEN** the application prints the help text and terminates normally

#### Scenario: Command-file execution of the JAR
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run with a text file containing commands
- **THEN** the application processes the commands of `input.txt` in order and terminates

#### Scenario: Build from scratch
- **WHEN** `mvn clean package` is run on a fresh checkout
- **THEN** the build succeeds and `target/amusement-park.jar` exists

### Requirement: Java standard library only
The application's runtime code SHALL depend only on the Java standard library: no databases, frameworks, external libraries or regular expressions are used.

#### Scenario: No runtime dependencies
- **WHEN** the project's dependencies are inspected
- **THEN** every declared dependency has `test` scope

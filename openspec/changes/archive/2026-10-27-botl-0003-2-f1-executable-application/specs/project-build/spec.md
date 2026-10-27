# Spec Delta

## ADDED Requirements

### Requirement: Executable JAR
Running `mvn package` SHALL produce an executable JAR in `target/` whose manifest declares the application entry point, so that it can be started with `java -jar` without an IDE or extra classpath configuration.

#### Scenario: JAR produced by package
- **WHEN** `mvn package` is executed at the repository root
- **THEN** the executable JAR `target/amusement-park.jar` exists

#### Scenario: Interactive execution
- **WHEN** `java -jar target/amusement-park.jar` is started without arguments and `HELP` then `EXIT` are typed on standard input
- **THEN** the application waits for and processes the commands from standard input, printing the help text and ending normally

#### Scenario: Command file execution
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run with a file containing `HELP` and `EXIT`
- **THEN** the application processes the commands contained in `input.txt` and ends normally

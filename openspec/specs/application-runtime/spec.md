# application-runtime Specification

## Purpose
Defines how the Amusement Park application is packaged and launched as a standalone executable JAR, which runtime technical restrictions it honors, and how it behaves in a controlled way when input cannot be processed.

## Requirements

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

### Requirement: Temporary persistence mode
When the first argument is `--fly`, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` and SHALL make every repository read and write only inside it, using the same repositories configured with different paths. When the run ends, whether through `EXIT` or after the command file or standard input is processed normally, the directory and all its files SHALL be deleted. Without `--fly`, arguments SHALL behave exactly as before and the normal persistent data SHALL be used.

#### Scenario: Fly mode with a command file
- **GIVEN** normal persistent data containing group `G01`
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` runs a file that creates group `G09` and ends with `EXIT`
- **THEN** `G01` was not visible during the run, and afterwards the normal data does not contain `G09` and the created `tmp/<timestamp>/` directory no longer exists

#### Scenario: Fly mode interactively
- **WHEN** `java -jar target/amusement-park.jar --fly` is run with commands on standard input ending in `EXIT`
- **THEN** the commands are processed against an empty temporary state and the temporary directory is deleted at the end

#### Scenario: Fly mode cleaned up at end of file without EXIT
- **WHEN** `--fly input.txt` processes a file without `EXIT`
- **THEN** the temporary directory is deleted after the last line

#### Scenario: Normal mode unchanged
- **WHEN** the JAR runs without `--fly`, either interactively or with a command file
- **THEN** the persistent data directory is used, and it is kept after the run ends

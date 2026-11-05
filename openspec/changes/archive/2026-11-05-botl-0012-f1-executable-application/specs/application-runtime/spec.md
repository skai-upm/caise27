# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR produced by Maven
Running `mvn package` on a clean checkout SHALL produce an executable JAR at `target/amusement-park.jar` whose manifest declares the application entry point class, so that it starts the application with `java -jar` with no IDE, source change or extra configuration. Started without arguments, the application SHALL run in interactive mode and wait for commands from standard input; started with a text file argument, it SHALL process that file.

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

#### Scenario: Entry point declared
- **GIVEN** the project `pom.xml`
- **WHEN** the JAR configuration is inspected after `mvn package`
- **THEN** the manifest `Main-Class` is the application entry point class, which declares `public static void main(String[])`

#### Scenario: Interactive mode waits for input
- **GIVEN** the packaged JAR started without arguments
- **WHEN** standard input provides `HELP`, then later `EXIT`
- **THEN** the application processes `HELP` and keeps waiting for the next command until `EXIT`

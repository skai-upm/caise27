# application-runtime Specification

## Purpose
Defines how the Amusement Park application is packaged and launched as a standalone executable JAR, which runtime technical restrictions it honors, and how it behaves in a controlled way when input cannot be processed.

## Requirements

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

### Requirement: Controlled handling of failing commands
Abnormal situations SHALL be represented by exceptions: malformed commands by an invalid-command exception from the presentation layer, and operations that business rules refuse by a park exception from the business layer. These exceptions SHALL propagate to command processing, which SHALL handle them in one place by reporting an error message for that line and continuing with the following lines. If executing a command fails unexpectedly in any other way, the application SHALL report an error message for that line and continue processing; the session SHALL NOT terminate abnormally.

#### Scenario: Failure inside one command
- **GIVEN** a command whose execution raises an unexpected error, followed by `HELP`
- **WHEN** the input is processed
- **THEN** an error message is shown for the failing line
- **AND** `HELP` is still executed

#### Scenario: Malformed command
- **GIVEN** `GROUP CREATE;G01` (missing a field) followed by `HELP`
- **WHEN** the input is processed
- **THEN** `ERROR: Invalid syntax. Usage: GROUP CREATE;<Identifier>;<Name>` is shown and `HELP` is still executed

#### Scenario: Operation refused by a business rule
- **GIVEN** group `G01` exists, and `GROUP CREATE;G01;Otra` is followed by `HELP`
- **WHEN** the input is processed
- **THEN** `ERROR: Group G01 already exists` is shown, `G01` is unchanged, and `HELP` is still executed

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

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep the Delivery 1 restrictions and the executable-JAR behavior. While loaded in memory, park relationships SHALL be held as objects and collections in these directions only: attractions know their occupants; tickets know their user; group tickets are composed of their individual tickets and reference their purchasing group (possibly empty); groups, users and attractions hold no ticket references. All ticket types SHALL share one abstraction, there SHALL be a single central-management instance, and `--fly` SHALL use the same repositories as normal mode, configured only with a different directory.

#### Scenario: Relationship directions
- **GIVEN** the domain classes
- **WHEN** their fields are inspected
- **THEN** `Group` and `Attraction` hold no ticket references, `Ticket` types hold no attraction references, and `GroupTicket` holds a collection of `IndividualTicket` and a `Group` reference

#### Scenario: Phase-2 command file in both modes
- **GIVEN** a phase-2 command file
- **WHEN** it is run normally and with `--fly`
- **THEN** both runs process all commands, the normal run persists its data, and the `--fly` run leaves no data behind

# application-execution Specification

## Purpose
Defines how the Amusement Park application is built and executed: a Maven-built executable JAR that runs without an IDE, interactively or from a command file, handling unprocessable input in a controlled manner.

## Requirements

### Requirement: Executable JAR built with Maven
Running `mvn package` on a clean checkout SHALL produce the JAR `target/amusement-park.jar`, whose manifest defines the application entry point, so that it can be launched with `java -jar` without an IDE, extra classpath or configuration.

#### Scenario: Build and launch from scratch
- **WHEN** `mvn package` is executed on a clean checkout and the produced JAR is launched with `java -jar target/amusement-park.jar` and input `EXIT`
- **THEN** the application starts, processes `EXIT` and terminates normally

#### Scenario: Entry point defined
- **WHEN** the manifest of `target/amusement-park.jar` is inspected
- **THEN** its `Main-Class` is the application entry point class, which declares `public static void main(String[])`

### Requirement: Interactive execution mode
When launched without arguments, the application SHALL read commands one per line from standard input until `EXIT` is processed or the input ends.

#### Scenario: Interactive HELP then EXIT
- **WHEN** the JAR is launched without arguments and receives the lines `HELP` and `EXIT` on standard input
- **THEN** the help listing is displayed and the application terminates normally

### Requirement: Command file execution mode
When launched with a file path argument, the application SHALL execute the commands of that text file one per line, in the order in which they appear in the file, stopping at `EXIT`.

#### Scenario: Commands executed in file order
- **WHEN** the JAR is launched with a file containing `FOO`, `HELP`, `EXIT`, `HELP`
- **THEN** the output shows the error for `FOO` before a single help listing and no line after `EXIT` is executed

#### Scenario: File without EXIT
- **WHEN** the JAR is launched with a file containing only `HELP`
- **THEN** the help listing is displayed and the application terminates normally at the end of the file

### Requirement: Controlled handling of unprocessable input
The application SHALL report situations it cannot process with an error message instead of terminating abnormally or printing a stack trace; a failing command SHALL NOT prevent the following commands from being processed.

#### Scenario: Missing command file
- **WHEN** the JAR is launched with a path to a file that does not exist
- **THEN** an error message naming the file is displayed, no stack trace is printed and the application terminates

#### Scenario: Command failure does not abort the session
- **WHEN** a command raises an unexpected error while being executed and is followed by `HELP`
- **THEN** an error message is displayed for the failing command and the help listing is still displayed

### Requirement: Delivery 1 technical restrictions
The application SHALL target Java 21 and parse commands with basic `String` operations and `split`, without regular expressions, databases or frameworks. External runtime libraries and encryption mechanisms SHALL NOT be used, except the jBCrypt library (`org.mindrot:jbcrypt`) introduced in Delivery 3 for password protection, which SHALL be packaged inside the executable JAR.

#### Scenario: No external runtime dependencies
- **WHEN** the project's `pom.xml` is inspected
- **THEN** every declared dependency has `test` scope except `org.mindrot:jbcrypt`, and the compiler release is 21

#### Scenario: Self-contained executable JAR
- **WHEN** `java -jar target/amusement-park.jar` runs an operation that protects a password
- **THEN** the BCrypt classes are found inside the JAR without any extra classpath

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create an independent temporary directory `tmp/<yyyyMMdd_HHmmss>/`, make every repository use only files inside it, run interactively or with the command file given as optional second argument, and delete the directory and all its files when execution ends through EXIT or when the input ends. The repositories SHALL be the same as in normal mode, differing only in configured paths. Without `--fly`, argument handling SHALL remain unchanged.

#### Scenario: Fly mode with a command file
- **WHEN** the JAR is run with `--fly input.txt` and the file creates attractions and ends with `EXIT`
- **THEN** the commands are executed, the permanent data files are not modified, and no `tmp/<timestamp>/` directory remains afterwards

#### Scenario: Fly mode interactively
- **WHEN** the JAR is run with `--fly` and receives `EXIT` on standard input
- **THEN** the temporary directory created at start-up no longer exists after the application ends

#### Scenario: Fly mode isolates from permanent data
- **WHEN** permanent data contains attraction `A01` and the JAR is run with `--fly` processing `ATTRACTION LIST;A01`
- **THEN** an error message reports that the attraction does not exist

### Requirement: Delivery 2 relationship directions
Relationships SHALL be held as object references and collections in memory with the mandated directions: attractions reference their occupants, individual tickets reference their user, group tickets compose their individual tickets and reference their group (nullable), while users hold no references to tickets, groups or attractions and groups hold no references to tickets. All ticket types SHALL share a common abstraction, and a single park controller instance SHALL exist.

#### Scenario: Users and groups do not reference tickets or attractions
- **WHEN** the fields of the user and group model classes are inspected
- **THEN** no field refers to tickets or attractions, and the user class has no field referring to groups

#### Scenario: Ticket abstraction
- **WHEN** the ticket model classes are inspected
- **THEN** individual and group tickets extend the same abstract ticket type

### Requirement: Delivery 2 end-to-end execution
A command file combining Delivery 1 and Delivery 2 commands SHALL execute in file order through the packaged application in normal mode and in `--fly` mode, with rejected operations reported and processing continuing, and `--fly` leaving no generated data behind.

#### Scenario: Full Delivery 2 command file
- **WHEN** a command file with system, group, ticket, group ticket and attraction commands (including invalid ones) and `EXIT` is executed with and without `--fly`
- **THEN** both runs produce the same output, the normal run persists its state, and the `--fly` run leaves no temporary directory

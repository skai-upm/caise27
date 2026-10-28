# application-execution Specification

## Purpose
Defines how the Amusement Park application is built and executed: a Maven-built executable JAR that runs without an IDE, interactively or from a command file, handling unprocessable input in a controlled manner.

## Requirements

### Requirement: Executable JAR built with Maven
Running `mvn package` on a clean checkout SHALL produce a JAR in `target/` that can be launched with `java -jar` without an IDE, extra classpath or configuration.

#### Scenario: Build and launch from scratch
- **WHEN** `mvn package` is executed on a clean checkout and the produced JAR is launched with `java -jar target/<jar>` and input `EXIT`
- **THEN** the application starts, processes `EXIT` and terminates normally

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
The application SHALL use only the Java standard library, target Java 21, and parse commands with basic `String` operations and `split`, without regular expressions, databases, frameworks, external runtime libraries or encryption mechanisms.

#### Scenario: No external runtime dependencies
- **WHEN** the project's `pom.xml` is inspected
- **THEN** every declared dependency has `test` scope and the compiler release is 21

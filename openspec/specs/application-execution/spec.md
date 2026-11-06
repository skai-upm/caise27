# application-execution Specification

## Purpose
Defines how the Amusement Park application is packaged and launched: an executable JAR built with Maven that runs without an IDE, in interactive mode or with a command file, and handles unprocessable input in a controlled way.

## Requirements

### Requirement: Executable JAR built with Maven
The project SHALL produce, with `mvn package` from a clean checkout, an executable JAR at `target/amusement-park.jar` that starts the application with `java -jar` and needs no IDE, extra classpath or other configuration. Its only third-party runtime library SHALL be the BCrypt library used for password protection, bundled inside the JAR.

#### Scenario: Package and run the JAR
- **GIVEN** a clean checkout with JDK 21+ and Maven
- **WHEN** `mvn package` is run and then `java -jar target/amusement-park.jar` is started with the input `EXIT`
- **THEN** the build succeeds, the application starts, and it terminates normally

#### Scenario: Runtime uses only the Java standard library
- **GIVEN** the project's dependency declarations
- **WHEN** the runtime classpath is resolved
- **THEN** it contains no third-party libraries other than `org.mindrot:jbcrypt` (test-only dependencies are allowed)

#### Scenario: BCrypt bundled in the JAR
- **WHEN** the contents of `target/amusement-park.jar` are listed
- **THEN** they include the `org/mindrot/jbcrypt/BCrypt.class` class

### Requirement: Interactive mode
The application SHALL, when started without arguments, read commands from standard input one line at a time and keep waiting for the next command after processing each one, until EXIT or end of input.

#### Scenario: Interactive session
- **GIVEN** the application started without arguments
- **WHEN** the user enters `HELP` and then `EXIT`
- **THEN** the help text is displayed and then the application terminates

### Requirement: File mode
The application SHALL, when started with a command file argument, treat it as the path to a text file with one command per line and execute the commands in the order in which they appear in the file. The command file argument is the first argument, or the second when the first is `--fly`.

#### Scenario: Commands from a file run in order
- **GIVEN** a file `input.txt` containing `FOO`, `HELP` and `EXIT` on separate lines
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** the unknown-command error for `FOO` is displayed before the help text
- **AND** the application terminates after `EXIT`

#### Scenario: Nonexistent command file
- **GIVEN** a path that does not exist or cannot be read
- **WHEN** the application is started with that path as its argument
- **THEN** an error message stating that the command file cannot be read is displayed
- **AND** the application terminates without a stack trace and without executing any command

#### Scenario: Command file after --fly
- **GIVEN** a file `input.txt` containing `HELP` and `EXIT`
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` is run
- **THEN** the commands of `input.txt` are executed in order

### Requirement: Blank lines are ignored
The application SHALL ignore lines that are empty or contain only whitespace in both interactive and file mode, without producing output or errors.

#### Scenario: Blank lines in a command file
- **GIVEN** a command file with blank lines between `HELP` and `EXIT`
- **WHEN** the file is processed
- **THEN** only the help text and the exit message are displayed, with no error for the blank lines

### Requirement: Controlled handling of processing failures
The application SHALL report an unexpected failure while processing a command line as an error message and SHALL continue processing the next line instead of terminating abnormally.

#### Scenario: A failing command does not stop the session
- **GIVEN** a command whose processing fails unexpectedly, followed by `HELP`
- **WHEN** both lines are processed
- **THEN** an error message is displayed for the failing line
- **AND** the help text is displayed for the next line

### Requirement: Supported execution forms
The application SHALL support exactly these argument forms: no arguments (interactive), `<file>` (file mode), `--fly` (interactive, temporary persistence) and `--fly <file>` (file mode, temporary persistence). Without `--fly`, argument behavior SHALL be the same as in Delivery 1.

#### Scenario: Interactive fly mode
- **WHEN** `java -jar target/amusement-park.jar --fly` is run with `HELP` and `EXIT` on standard input
- **THEN** the help text is displayed and the application terminates normally

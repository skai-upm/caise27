# executable-application Specification

## Purpose

Defines how the Amusement Park application is packaged by Maven as an executable JAR and launched from a terminal independently of any IDE.

## Requirements

### Requirement: Executable JAR packaging
Running `mvn package` SHALL produce the file `target/amusement-park.jar`, and its manifest SHALL declare the application entry-point class as `Main-Class`, so that it can be launched with `java -jar target/amusement-park.jar` without an IDE or additional classpath configuration.

#### Scenario: Package produces an executable JAR
- **GIVEN** the Maven project in a clean checkout
- **WHEN** `mvn package` is executed
- **THEN** the build succeeds and `target/amusement-park.jar` exists
- **AND** its manifest contains a `Main-Class` attribute naming the entry-point class

#### Scenario: JAR starts outside the IDE
- **GIVEN** the packaged `target/amusement-park.jar`
- **WHEN** it is launched from a terminal with `java -jar target/amusement-park.jar`
- **THEN** the application starts without `no main manifest attribute` or class-loading errors

#### Scenario: Failing build produces no new executable JAR
- **GIVEN** sources that do not compile
- **WHEN** `mvn package` is executed
- **THEN** the build fails and no executable JAR is produced by that build

### Requirement: Interactive mode on standard input
When launched without arguments, the application SHALL start in interactive mode, waiting for commands read line by line from standard input, SHALL process each line read as a command and print its response to standard output, and SHALL terminate normally (exit status 0) when standard input reaches its end.

#### Scenario: Application waits for input
- **GIVEN** the application launched with `java -jar target/amusement-park.jar` and no arguments
- **WHEN** no input has been provided yet
- **THEN** the process remains running, waiting to read from standard input

#### Scenario: End of input terminates normally
- **GIVEN** the application running in interactive mode
- **WHEN** standard input is closed (end of input)
- **THEN** the application terminates with exit status 0

#### Scenario: Lines are processed as commands
- **GIVEN** the application running in interactive mode
- **WHEN** a valid `USER CREATE` line is typed
- **THEN** the response `USER CREATED` is printed to standard output before the next line is read

### Requirement: File mode
When launched with a file path as its first argument (`java -jar target/amusement-park.jar input.txt`), the application SHALL read the file as one command per line, process the commands in the order in which they appear, ignore blank lines, and terminate normally after the last line.

#### Scenario: Commands processed in file order
- **GIVEN** `input.txt` containing a `USER CREATE` for Ana, a blank line, and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** `USER CREATED` then `USER UPDATED` are printed in that order and the application exits with status 0

#### Scenario: Errors in the file do not stop processing
- **GIVEN** a file whose first line is an unknown command and whose second line is a valid `USER CREATE`
- **WHEN** the file is processed
- **THEN** an `ERROR` message is printed for the first line and `USER CREATED` for the second

### Requirement: Identical processing for both input sources
Command processing SHALL be identical whether commands come from standard input or from a file: the same command in the same state SHALL produce the same effect and response.

#### Scenario: Same response from file and interactive input
- **GIVEN** the same sequence of commands supplied once on standard input and once as a file, each starting from an empty users file
- **WHEN** both runs complete
- **THEN** both print the same responses and leave the same stored users

### Requirement: Controlled handling of unusable command file
If the file given as argument does not exist or cannot be read, the application SHALL print an error message starting with `ERROR` that identifies the file and terminate in a controlled manner with exit status 1, without an uncaught exception or stack trace and without processing any command.

#### Scenario: Nonexistent file
- **WHEN** `java -jar target/amusement-park.jar missing.txt` is run and `missing.txt` does not exist
- **THEN** an `ERROR` message naming `missing.txt` is printed, no stack trace is shown and the exit status is 1

#### Scenario: Unreadable file
- **GIVEN** a path that cannot be read as a file (for example a directory)
- **WHEN** the application is launched with that path
- **THEN** an `ERROR` message is printed and the application terminates with exit status 1

### Requirement: Temporary persistence mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique if it already exists), make all repositories use files only inside it, and delete it with all its files when the execution ends normally (EXIT, end of standard input or end of the command file). Both interactive and file mode SHALL be supported with `--fly`; persistent files outside the temporary directory MUST NOT be read or modified. Without `--fly`, argument handling is unchanged.

#### Scenario: Interactive fly mode leaves persistent data untouched
- **GIVEN** a `users.txt` containing Ana in the working directory
- **WHEN** `java -jar target/amusement-park.jar --fly` is run with `USER CREATE` for Pedro and `EXIT`
- **THEN** `USER CREATED` is printed, `users.txt` still contains only Ana, and no `tmp/<timestamp>` directory remains

#### Scenario: Fly mode starts from an empty state
- **GIVEN** a `users.txt` containing Ana in the working directory
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` is run where `input.txt` creates Ana
- **THEN** `USER CREATED` is printed (Ana is not loaded from the permanent files) and the temporary directory is deleted at the end

#### Scenario: Normal forms unchanged
- **WHEN** the application is run with no arguments or with only a command file
- **THEN** it reads and writes the permanent files in the working directory as before

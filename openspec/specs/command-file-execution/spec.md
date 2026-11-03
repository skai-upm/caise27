# command-file-execution Specification

## Purpose

Runs commands from standard input or from a command file through one shared processing path so a command behaves identically regardless of its source.

## Requirements

### Requirement: Interactive mode
Without arguments, the application SHALL read commands from standard input one line at a time and, after processing each, wait for the next until `EXIT` or end of input.

#### Scenario: Interactive sequence
- **WHEN** a `USER CREATE` followed by `EXIT` is typed on standard input
- **THEN** the user is created and the application then terminates

### Requirement: File mode
When a file path is given as the first argument, the application SHALL execute the file's commands, one per line, in the order they appear, ignoring blank lines.

#### Scenario: Ordered file execution
- **WHEN** a file creates a user and then updates that user's phone
- **THEN** both commands execute in order and the phone is updated

#### Scenario: Blank lines ignored
- **WHEN** the file contains blank lines between commands
- **THEN** blank lines produce no output or error

### Requirement: Identical processing for both sources
Commands from standard input and from a file SHALL be parsed and executed by the same command-processing implementation, producing the same state changes and results.

#### Scenario: Equivalent results
- **WHEN** the same command sequence runs in interactive mode and in file mode from equivalent initial states
- **THEN** the resulting users and outputs are equivalent

### Requirement: Controlled handling of unreadable files
A nonexistent or unreadable command file SHALL be reported with an error message, and the application SHALL terminate without an uncaught exception.

#### Scenario: Missing file
- **WHEN** the JAR is started with a path that does not exist
- **THEN** an error message is printed and no stack trace is shown

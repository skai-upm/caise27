# command-file-execution Specification

## Purpose

Makes commands behave identically whether typed interactively or read from a command file passed at start-up, using a single command processing path (source F1-C07, sections 10.1 and 10.2).

## Requirements

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input one line at a time and, after processing each command, continue waiting for the next one until `EXIT` or end of input.

#### Scenario: Interactive sequence processed
- **WHEN** the application is started without arguments and receives a `USER CREATE` command followed by `USER UPDATE` on standard input
- **THEN** both commands are processed in order and the application keeps reading input

### Requirement: File mode
When started with a file path as its first argument, the application SHALL read the file as one command per line, process the commands in the order in which they appear, and ignore blank lines.

#### Scenario: Commands processed in file order
- **WHEN** a file containing two `USER CREATE` lines, a blank line and a `USER UPDATE` line is passed as the first argument
- **THEN** the two users are created and then the update is applied, and the blank line produces no output or error

### Requirement: Identical processing regardless of source
Interactive and file input SHALL be handled by the same command parsing and execution, so the same command sequence from equivalent initial states produces the same results and state changes in both modes.

#### Scenario: Equivalent results in both modes
- **WHEN** the same command sequence is executed once interactively and once from a file, each from an empty user store
- **THEN** the reported results and the resulting stored users are equivalent

### Requirement: Controlled handling of missing or unreadable command files
A nonexistent or unreadable command file SHALL be reported with an error message, without an uncaught exception or stack trace.

#### Scenario: Nonexistent file
- **WHEN** the application is started with a path to a file that does not exist
- **THEN** an error message is reported and the application terminates in a controlled manner without an uncaught exception

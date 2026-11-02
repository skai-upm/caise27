# command-file-execution Specification

## Purpose

Defines how commands are read interactively from standard input or from a command file, and guarantees identical processing whichever source is used (F1-C07).

## Requirements

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input, one per line, and after processing each command SHALL wait for the next one until `EXIT` or end of input.

#### Scenario: Sequence on standard input
- **WHEN** the application is started without arguments and receives a valid `USER CREATE` line followed by `EXIT`
- **THEN** the user is created and the application terminates in a controlled manner

### Requirement: File mode
When started with a text file path as first argument, the application SHALL process the file's commands, one per line, in the order in which they appear, ignoring blank lines.

#### Scenario: Ordered processing with blank lines
- **WHEN** a file contains two `USER CREATE` lines separated by a blank line, followed by a `USER UPDATE` for the first user
- **THEN** both users are created and the first user is updated, and the blank line produces no error

### Requirement: Identical processing for both sources
Commands from standard input and from a file SHALL be processed by the same command-processing implementation, so that the observable behaviour of a command does not depend on its source.

#### Scenario: Equivalent state from both modes
- **WHEN** the same command sequence is run once interactively and once from a file, starting from equivalent initial states
- **THEN** both runs produce the same outputs and the same final users

### Requirement: Controlled handling of unusable command files
A nonexistent or unreadable command file SHALL be reported as an error in a controlled manner, without an uncaught exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an error message is reported and the application ends without an uncaught exception and without modifying stored users

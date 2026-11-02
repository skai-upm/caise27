# command-file-execution Specification

## Purpose

Executes commands either interactively from standard input or from a text file given as the first argument, through one shared command-processing pathway with identical observable behavior.

## Requirements

### Requirement: Interactive mode
When the application is started without arguments it SHALL read commands from standard input, one per line, and after processing each command SHALL wait for the next one until `EXIT` or end of input.

#### Scenario: Several interactive commands
- **WHEN** `USER CREATE;...` followed by `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` are entered interactively
- **THEN** both are processed in order and the application waits for the next command

### Requirement: File mode
When the application is started with a file path as its first argument it SHALL process the file's commands, one per line, in the order in which they appear, ignoring blank lines.

#### Scenario: Ordered file processing with blank lines
- **WHEN** a file containing a `USER CREATE`, a blank line and a `USER UPDATE` for the same user is processed
- **THEN** the create is executed before the update and the blank line produces no output or error

### Requirement: Single shared command processing
Interactive and file mode SHALL use the same command interpretation and execution, so a given command sequence produces the same observable results and state changes regardless of its source.

#### Scenario: Equivalent results
- **WHEN** the same command sequence is executed interactively and from a file, starting from equivalent stored states
- **THEN** the outputs and resulting stored users are identical

### Requirement: Controlled handling of unusable command files
A nonexistent or unreadable command file SHALL be reported with an error message, without an uncaught exception or stack trace.

#### Scenario: Missing file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an error message naming the problem is printed and the application terminates in a controlled manner

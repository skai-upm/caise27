# Spec Delta

## ADDED Requirements

### Requirement: Command file input
When started with at least one argument, the application SHALL treat the first argument as the path of a text file containing one command per line, process its commands in the order in which they appear, ignore blank lines, print each command's response to standard output, and terminate normally when the end of the file is reached. Standard input is not read in this mode.

#### Scenario: Commands from file processed in order
- **WHEN** the application is started with `input.txt` containing `USER CREATE` for Ana, a blank line, `USER CREATE` for Pedro and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`
- **THEN** three responses starting with `OK` are printed in that order
- **AND** Ana's telephone is `699999999`
- **AND** the application terminates with exit status 0

#### Scenario: Errors in the file do not stop processing
- **WHEN** a command file contains an invalid command followed by a valid `USER CREATE`
- **THEN** an `ERROR` response is printed for the invalid command and the following command is still processed

### Requirement: Same behavior for every command source
Commands read from a file and commands read from standard input SHALL be processed by the same command-processing logic and SHALL produce identical responses and state changes for identical input lines.

#### Scenario: Identical output for both sources
- **WHEN** the same sequence of command lines is given once through standard input and once as a command file, each starting from the same stored users
- **THEN** the printed responses are identical and the resulting stored users are identical

### Requirement: Unreadable command file handled
If the command file given as the first argument does not exist or cannot be read, the application SHALL print an error message starting with `ERROR` that names the file, MUST NOT terminate with an uncaught exception, and MUST NOT execute any command or modify stored users.

#### Scenario: Nonexistent file
- **WHEN** the application is started with the argument `missing.txt` and no such file exists
- **THEN** an `ERROR` message mentioning `missing.txt` is printed
- **AND** no stack trace is printed and the stored users are unchanged

#### Scenario: Unreadable path
- **WHEN** the argument names a directory instead of a regular file
- **THEN** an `ERROR` message mentioning the path is printed and the application terminates without an uncaught exception

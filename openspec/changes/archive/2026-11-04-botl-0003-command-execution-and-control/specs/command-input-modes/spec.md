# Spec Delta

## Purpose

Defines where the application reads its commands from (terminal or command file), in what order they are processed, and how input problems are handled.

## ADDED Requirements

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input, one per line, process each one, and wait for the next until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT`
- **THEN** the user is created, the phone is updated, the help is shown and the application terminates after `EXIT`

#### Scenario: End of standard input
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application terminates normally after processing all received commands

### Requirement: File mode
When started with at least one argument, the application SHALL treat the first argument as the path of a text file, read it line by line, and process each non-blank line as a command in the order the lines appear, until it processes `EXIT` or the file ends. Blank lines SHALL be skipped without error.

#### Scenario: File with commands in order
- **WHEN** the application is started with a file containing a creation of Ana, a creation of Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **THEN** both users exist and Ana's phone is `699999999`

#### Scenario: Blank lines in the file
- **WHEN** the file contains empty or whitespace-only lines between valid commands
- **THEN** those lines are skipped and the valid commands are executed

### Requirement: Shared command processing
Interactive mode and file mode SHALL use the same command interpretation and execution, so that the same sequence of commands produces the same output and the same persisted effects in both modes.

#### Scenario: Equivalent runs
- **WHEN** the same sequence of commands is run once through standard input and once through a command file, each starting from the same stored users
- **THEN** both runs print the same result lines and leave the same stored users

### Requirement: Controlled handling of command-file errors
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print an `ERROR` message naming the file and terminate without executing further commands and without showing an unhandled Java exception.

#### Scenario: Missing file
- **WHEN** the application is started with a path that does not exist
- **THEN** it prints an `ERROR` message mentioning that path and terminates without a stack trace

#### Scenario: Path is not a readable file
- **WHEN** the application is started with a path that is a directory or contains bytes that are not valid UTF-8
- **THEN** it prints an `ERROR` message and terminates without a stack trace

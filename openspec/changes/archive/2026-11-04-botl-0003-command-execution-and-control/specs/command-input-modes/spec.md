# Spec Delta

## Purpose

Defines how commands reach the application — interactively from standard input or from a command file given as the first argument — and guarantees both sources share one processing path.

## ADDED Requirements

### Requirement: Interactive mode without arguments
When started without arguments the application SHALL read commands from standard input, one per line, processing each before reading the next, until `EXIT` is processed or standard input ends.

#### Scenario: Interactive session
- **GIVEN** the application started without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP`, `EXIT` are entered
- **THEN** the user is created, its phone becomes `699999999`, the help is shown, and the application terminates after `EXIT` with exit status 0

#### Scenario: End of input
- **GIVEN** the application started without arguments
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally with exit status 0

### Requirement: File mode with one argument
When started with at least one argument the application SHALL interpret the first argument as the path of a text file, read it line by line and process each line as a command in the order it appears.

#### Scenario: Commands executed in file order
- **GIVEN** a file with the lines `USER CREATE;Ana;…`, `USER CREATE;Pedro;…`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `EXIT`
- **WHEN** the application is started with that file path as first argument
- **THEN** both users are created, Ana's phone is `699999999`, and the application terminates

### Requirement: Blank lines ignored
Lines that are empty or contain only whitespace SHALL be skipped without producing any response or error, in both modes.

#### Scenario: Blank lines between commands
- **GIVEN** a file containing a `USER CREATE` line, an empty line, `HELP` and `EXIT`
- **WHEN** it is executed
- **THEN** the user is created, the help is shown, and no error is reported for the empty line

### Requirement: Shared processing for both modes
Commands from standard input and from a file SHALL be interpreted and executed by the same processing, so the same sequence of commands yields the same responses and the same persisted effects in either mode.

#### Scenario: Equivalent runs
- **GIVEN** two empty data locations
- **WHEN** the same command sequence is executed once from standard input and once from a file
- **THEN** the printed responses are identical and the resulting user files are identical

### Requirement: Controlled handling of unreadable files
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print an `ERROR:` message naming the file to standard error and terminate with exit status 1, without printing an unhandled exception or stack trace.

#### Scenario: Missing file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an `ERROR:` message naming the path is printed, no command is executed, the exit status is 1 and no stack trace appears

#### Scenario: Path is a directory
- **WHEN** the application is started with the path of a directory
- **THEN** an `ERROR:` message is printed, the exit status is 1 and no stack trace appears

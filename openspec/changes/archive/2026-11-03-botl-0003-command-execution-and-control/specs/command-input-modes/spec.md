# Spec Delta

## Purpose

Lets commands be supplied either interactively through standard input or in batch from a text file, with identical processing in both cases.

## ADDED Requirements

### Requirement: Interactive mode
When launched without arguments, the application SHALL read commands from standard input, one per line, process each line as soon as it is read, and keep waiting for further lines until `EXIT` is processed or the input ends.

#### Scenario: Interactive session
- **GIVEN** the application started without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP`, `EXIT` are entered
- **THEN** the user is created, the phone is updated, the help is shown, and the application terminates after `EXIT`

#### Scenario: End of input
- **GIVEN** the application started without arguments
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally after processing every line received

### Requirement: File mode
When launched with at least one argument, the application SHALL treat the first argument as the path of a text file, read it line by line and process each non-blank line as a command, in file order.

#### Scenario: Batch file run
- **GIVEN** a file `input_basico.txt` with lines creating Ana and Pedro, updating Ana's phone to `699999999`, then `EXIT`
- **WHEN** the application is launched with `input_basico.txt`
- **THEN** both users exist and Ana's phone is `699999999`

#### Scenario: Blank lines are skipped
- **GIVEN** a file with an empty line between a valid `USER CREATE` and `HELP`
- **WHEN** it is processed
- **THEN** the empty line produces no output and no error, and both commands are executed

### Requirement: Shared command processing
Interactive and file modes SHALL use the same command-processing mechanism, so that the same sequence of commands produces the same responses and the same persisted effects from either source.

#### Scenario: Equivalent results
- **GIVEN** the same command sequence supplied once via standard input and once via a file, each starting from an empty user file
- **WHEN** both runs finish
- **THEN** their printed responses and resulting user files are identical

### Requirement: Invalid command file
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print an error message naming the file and terminate without executing commands from it and without showing an unhandled exception.

#### Scenario: Missing file
- **GIVEN** a path that does not exist
- **WHEN** the application is launched with it
- **THEN** an `ERROR:` message about the file is printed, no stack trace is shown, and the application ends

#### Scenario: Path is a directory
- **GIVEN** a path that names a directory
- **WHEN** the application is launched with it
- **THEN** an `ERROR:` message about the file is printed and the application ends in a controlled way

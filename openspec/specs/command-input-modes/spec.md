# command-input-modes Specification

## Purpose
Defines how the application obtains commands — interactively from standard input or from a command file given as the first launch argument — and guarantees both sources share identical processing.

## Requirements

### Requirement: Interactive mode
When launched without a command-file argument (no arguments, or only the `--fly` option), the application SHALL read commands from standard input, one command per line, and after each non-EXIT command SHALL wait for the next line until `EXIT` is processed or standard input ends.

#### Scenario: Interactive session
- **GIVEN** the application starts without arguments
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT` are entered
- **THEN** the create, update and help results are printed, the application stops after `EXIT` and Ana's phone is persisted as `699999999`

#### Scenario: End of input without EXIT
- **GIVEN** interactive mode
- **WHEN** standard input ends after a valid command
- **THEN** the command is processed and the application terminates normally

#### Scenario: Interactive with --fly
- **WHEN** the application is launched with only `--fly`
- **THEN** it runs in interactive mode on temporary data

### Requirement: File mode
When launched with an argument other than the `--fly` option, the application SHALL treat the first such argument as the path of a text command file, read it line by line and execute each line as a command in the order it appears.

#### Scenario: Basic input file
- **GIVEN** a file `input_basico.txt` containing the creation of Ana and Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **WHEN** the application is launched with `input_basico.txt` as first argument
- **THEN** both users are created, Ana's phone becomes `699999999`, and processing ends at `EXIT`

#### Scenario: File mode with --fly
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file on temporary data

### Requirement: Blank lines skipped
Blank or whitespace-only lines SHALL be ignored in both modes: they produce no result message and no error.

#### Scenario: Blank lines between commands
- **GIVEN** a command file with an empty line between a valid `USER CREATE` and `HELP`, followed by `EXIT`
- **WHEN** the file is processed
- **THEN** the create and help results are printed, and no error is reported for the empty line

### Requirement: Shared processing
Interactive and file modes SHALL use the same command loop and the same command processor, so the same sequence of commands yields the same printed results and persisted effects from either source.

#### Scenario: Equivalent runs
- **GIVEN** two fresh users files
- **WHEN** the same command sequence is run once through standard input and once through a command file
- **THEN** the printed results are identical and both users files have identical contents except for the salted password hashes, which verify the same passwords

### Requirement: Controlled command-file errors
If the command file does not exist, is not a regular file, cannot be opened or cannot be read, the application SHALL print a single error message naming the file, SHALL NOT print an unhandled Java exception, and SHALL terminate with a non-zero exit status without executing any further command.

#### Scenario: Missing file
- **WHEN** the application is launched with a path that does not exist
- **THEN** an `ERROR` message naming the path is printed, no stack trace appears and the exit status is non-zero

#### Scenario: Directory instead of file
- **WHEN** the application is launched with a path that is a directory
- **THEN** an `ERROR` message is printed and the application terminates in a controlled manner

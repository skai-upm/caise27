# command-input-modes Specification

## Purpose

Defines how commands reach the application, interactively from standard input or from a command file named on launch, and guarantees both sources share one processing path with identical results.

## Requirements

### Requirement: Interactive mode
When launched without a command-file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input one line at a time, print one result per non-blank line, and keep waiting for further commands until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session ends with EXIT
- **GIVEN** the application is started without arguments
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT` are entered
- **THEN** the create, update and help are processed in order and the application terminates normally after `EXIT` without reading further input

#### Scenario: End of input
- **GIVEN** the application is started without arguments
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally after processing the commands received

#### Scenario: Interactive with --fly
- **WHEN** the application is started with only `--fly`
- **THEN** it reads commands from standard input

### Requirement: File mode
When launched with at least one argument other than `--fly`, the application SHALL treat the first such argument as the path of a text file of commands, read it line by line, and process each non-blank line as a command in the order it appears.

#### Scenario: File with commands in order
- **GIVEN** a file with lines creating Ana, creating Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **WHEN** the application is started with that file path as first argument
- **THEN** both users are created, Ana's phone becomes `699999999`, and the application terminates normally

#### Scenario: Blank lines are ignored
- **GIVEN** a file with an empty line and a whitespace-only line between valid commands
- **WHEN** it is processed
- **THEN** the blank lines produce no output and no error, and the valid commands execute

#### Scenario: File after --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file

### Requirement: Shared command processing
Interactive mode and file mode SHALL use the same command interpretation and execution, so the same command sequence produces the same output and the same persisted effects from either source.

#### Scenario: Equivalent results
- **GIVEN** the same sequence of commands supplied once through standard input and once through a file, each starting with no stored users
- **WHEN** both runs complete
- **THEN** their printed results are identical and their persisted user files are identical

### Requirement: Controlled command-file errors
If the command file does not exist, cannot be opened or cannot be read, the application SHALL print an error message naming the file, process no further commands, and terminate with a nonzero exit status without displaying an uncaught exception.

#### Scenario: Missing file
- **WHEN** the application is started with a path that does not exist
- **THEN** it prints an error naming the path, shows no Java stack trace, and exits with a nonzero status

#### Scenario: Unreadable path
- **WHEN** the application is started with a path that is a directory
- **THEN** it prints an error naming the path, shows no Java stack trace, and exits with a nonzero status

# Spec Delta

## MODIFIED Requirements

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

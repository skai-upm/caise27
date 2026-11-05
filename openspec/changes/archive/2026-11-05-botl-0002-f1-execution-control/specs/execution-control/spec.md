# Spec Delta

## Purpose

Defines the general session commands HELP and EXIT and the uniform, line-by-line processing of commands regardless of whether they come from standard input or from a command file.

## ADDED Requirements

### Requirement: Uniform command processing across input sources
The application SHALL process commands one per line, in order, through a single processing path shared by standard input (started without arguments) and a command file (path given as first argument). Blank lines SHALL be ignored. An unrecognized line SHALL produce an error message and processing SHALL continue with the next line.

#### Scenario: Same output from stdin and file
- **GIVEN** the lines `HELP` and `EXIT`
- **WHEN** they are processed once from standard input and once from a command file
- **THEN** the produced output is identical in both cases

#### Scenario: Blank lines ignored
- **GIVEN** an input containing empty or whitespace-only lines between commands
- **WHEN** the input is processed
- **THEN** blank lines produce no output and no error

#### Scenario: Unknown command reported without stopping
- **GIVEN** an input `FOO` followed by `HELP`
- **WHEN** the input is processed
- **THEN** an error message identifying the unknown command is shown
- **AND** the `HELP` command is still executed

### Requirement: HELP command
The command `HELP` SHALL display the available commands and their basic syntax, including at least: `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help lists the mandatory commands
- **GIVEN** interactive or file input
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT` exactly as listed above

#### Scenario: Help does not change state or stop processing
- **GIVEN** an input `HELP` followed by `HELP`
- **WHEN** the input is processed
- **THEN** the help text is displayed twice and processing continues

### Requirement: EXIT command
The command `EXIT` SHALL terminate command processing in a controlled manner: the application ends normally and no line after `EXIT` SHALL be processed, regardless of the input source.

#### Scenario: Exit stops a command file
- **GIVEN** a file containing `HELP`, `EXIT`, `HELP`
- **WHEN** the file is processed
- **THEN** the help text is displayed exactly once
- **AND** the line after `EXIT` is not processed

#### Scenario: Exit stops interactive input
- **GIVEN** standard input providing `EXIT` and then further commands
- **WHEN** the application reads `EXIT`
- **THEN** the application terminates normally without reading or executing any further command

#### Scenario: End of input without EXIT
- **GIVEN** an input whose last line is `HELP` with no `EXIT`
- **WHEN** the end of the input is reached
- **THEN** the application terminates normally

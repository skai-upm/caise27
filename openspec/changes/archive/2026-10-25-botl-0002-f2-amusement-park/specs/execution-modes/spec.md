# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode without arguments
When started without a command-file argument (with no arguments, or only the `--fly` option), the application SHALL read commands line by line from standard input until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application runs without arguments and receives `HELP` and then `EXIT` on standard input
- **THEN** it prints the help text and terminates after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application terminates normally

#### Scenario: Only the fly option
- **WHEN** the application is started with `--fly` only
- **THEN** it reads commands from standard input

### Requirement: File mode with one argument
When started with a non-option argument, the application SHALL treat the first such argument as the path of a text file, read it line by line and execute each line as a command in order, stopping at `EXIT`. The `--fly` option MAY appear together with the file argument and does not count as the file path.

#### Scenario: Commands from file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** both users are created, the update is applied, and processing ends at `EXIT`

#### Scenario: Commands after EXIT are ignored
- **WHEN** a command file contains `EXIT` followed by a `USER CREATE` line
- **THEN** the `USER CREATE` line is not executed

#### Scenario: File combined with fly option
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is read as the command file

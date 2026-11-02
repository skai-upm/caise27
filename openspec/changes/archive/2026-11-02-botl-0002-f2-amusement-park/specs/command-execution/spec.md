# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When launched without a command-file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input, one command per line, processing each before reading the next, until `EXIT` is processed or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is launched without arguments and the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `HELP`, `EXIT` are entered
- **THEN** the user is created, the help text is shown, and the application terminates normally

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally

#### Scenario: Interactive fly mode
- **WHEN** the application is launched with only `--fly`
- **THEN** commands are read from standard input

### Requirement: File mode
When launched with a command-file argument — the first argument, or the second when the first is `--fly` — the application SHALL treat it as the path of a text file, read it line by line, and process each non-blank line as a command in file order. Blank lines SHALL be ignored.

#### Scenario: Ordered file processing with blank lines
- **WHEN** a file contains valid commands interleaved with blank lines
- **THEN** the non-blank commands are executed in file order and the blank lines cause no error

#### Scenario: File after fly flag
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are executed in file mode

### Requirement: HELP command
The `HELP` command SHALL display every available command with its basic syntax, consistent with the commands actually implemented: the F1 user commands, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, `HELP`, and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is processed
- **THEN** every command listed in this requirement is displayed with its syntax

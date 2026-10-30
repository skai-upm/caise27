# command-interface Specification

## Purpose

Defines the `;`-separated text command interface, how commands are read interactively or from a file, the HELP and EXIT commands, and controlled handling of invalid input.

## Requirements

### Requirement: Command syntax
Commands SHALL consist of elements separated by `;` and be interpreted using basic String operations including split, without external libraries. The recognised commands are `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`.

#### Scenario: Unknown command
- **WHEN** a line such as `FOO BAR` or `USER LIST` is processed
- **THEN** an `ERROR:` message is printed and the application keeps running

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands line by line from standard input, process each one and then wait for the next, until EXIT is processed or the input ends.

#### Scenario: Several interactive commands
- **WHEN** a USER CREATE, a USER UPDATE, HELP and EXIT are typed in sequence
- **THEN** each command's result is printed in order and the application terminates after EXIT

### Requirement: File mode
When started with a file path as first argument, the application SHALL process the file's lines in order as commands, ignoring blank lines, with exactly the same processing as interactive mode.

#### Scenario: File commands processed in order
- **WHEN** a file contains two USER CREATE lines, a blank line and a USER UPDATE line
- **THEN** the two users are created and then the update is applied, and the blank line produces no output

#### Scenario: Missing or unreadable file
- **WHEN** the application is started with a path that does not exist or cannot be read
- **THEN** an `ERROR:` message is printed and the application terminates without an unhandled exception

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least `USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>`, `USER DELETE;<email>;<password>`, `USER UPDATE;<email>;<password>;<field>;<value>`, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** each of the five command syntaxes above is printed

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner, and no subsequent command SHALL be executed in either mode.

#### Scenario: EXIT stops file processing
- **WHEN** a file contains `USER CREATE;...` (user A), `EXIT`, `USER CREATE;...` (user B)
- **THEN** user A is created and user B is not

### Requirement: Controlled handling of invalid input
No command, however malformed, SHALL cause the application to terminate unexpectedly.

#### Scenario: Malformed commands do not crash
- **WHEN** lines such as `USER`, `USER CREATE`, `;;;` or `USER UPDATE;a;b` are processed
- **THEN** an `ERROR:` message is printed for each and processing continues

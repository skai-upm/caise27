# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When started without a command file (no arguments, or only `--fly`), the application SHALL read commands line by line from standard input, process each one, and keep waiting for the next command until `EXIT` is received or input ends.

#### Scenario: Commands from standard input
- **WHEN** the JAR is started without arguments and the user types `HELP` followed by `EXIT`
- **THEN** the help text is printed and the application then terminates normally

#### Scenario: End of input
- **WHEN** standard input is closed without an `EXIT` command
- **THEN** the application terminates normally

#### Scenario: Interactive temporary mode
- **WHEN** the JAR is started with `--fly` as the only argument
- **THEN** commands are read from standard input using temporary persistence

### Requirement: File mode
When started with a command file path as first argument, or as second argument after `--fly`, the application SHALL process the commands in that file, one per line, in order, ignoring blank lines, with exactly the same observable behaviour per command as in interactive mode.

#### Scenario: Commands from a file
- **WHEN** the JAR is run with `input.txt` containing two `USER CREATE` lines and an `EXIT` line
- **THEN** both users are created in file order and the application terminates

#### Scenario: Blank lines ignored
- **WHEN** the command file contains empty or whitespace-only lines between commands
- **THEN** those lines produce no output and no error

#### Scenario: File in temporary mode
- **WHEN** the JAR is run with `--fly input_fase2.txt`
- **THEN** the commands in `input_fase2.txt` are processed using temporary persistence

## ADDED Requirements

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new, empty directory `tmp/<yyyyMMdd_HHmmss>` (made unique if it already exists) and use it as the data directory for all persistent elements (users, groups, tickets, attractions) during that execution. When execution ends — through `EXIT`, end of the command file or end of standard input — the directory and all its files SHALL be deleted, together with `tmp/` if it was created by this execution and is left empty. Without `--fly`, the normal data directory is used and argument handling is unchanged.

#### Scenario: Persistent data untouched
- **WHEN** `users.txt` exists in the working directory and the JAR is run with `--fly` and a file that creates users
- **THEN** the users are created during that run, the existing `users.txt` is unchanged, and no temporary files remain after it ends

#### Scenario: Fresh state
- **WHEN** the JAR is run with `--fly`
- **THEN** it starts with no users, groups, tickets or attractions

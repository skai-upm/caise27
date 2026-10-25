# execution-modes Specification

## Purpose

Defines how commands are supplied to the application: interactively from standard input or from a command file.

## Requirements

### Requirement: Interactive mode without arguments
When started without arguments, the application SHALL read commands line by line from standard input until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application runs without arguments and receives `HELP` and then `EXIT` on standard input
- **THEN** it prints the help text and terminates after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application terminates normally

### Requirement: File mode with one argument
When started with an argument, the application SHALL treat the first argument as the path of a text file, read it line by line and execute each line as a command in order, stopping at `EXIT`.

#### Scenario: Commands from file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** both users are created, the update is applied, and processing ends at `EXIT`

#### Scenario: Commands after EXIT are ignored
- **WHEN** a command file contains `EXIT` followed by a `USER CREATE` line
- **THEN** the `USER CREATE` line is not executed

### Requirement: Shared processing across modes
Interactive mode and file mode SHALL use the same command processing, so the same command yields the same result in both modes.

#### Scenario: Same output in both modes
- **WHEN** the same sequence of commands is supplied once through standard input and once through a file, starting from the same stored data
- **THEN** each command produces the same result message

### Requirement: Blank lines ignored
Blank or whitespace-only lines SHALL be ignored without error in both modes.

#### Scenario: Blank line in file
- **WHEN** a command file contains an empty line between two commands
- **THEN** both commands are executed and no error is reported for the blank line

### Requirement: Unreadable command file handled
If the command file does not exist, cannot be opened or cannot be read, the application SHALL report the problem with an `ERROR` message and terminate in a controlled manner without displaying an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with `missing.txt` that does not exist
- **THEN** an `ERROR` message naming the file is printed and the application ends without a stack trace

# command-execution Specification

## Purpose

Defines how commands reach the application—interactively from standard input or from a command file—and the general HELP and EXIT commands, with controlled handling of invalid input.

## Requirements

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

### Requirement: Shared command processing
Interactive and file modes SHALL use the same command interpretation and execution so that the same command produces the same observable result in either mode.

#### Scenario: Equal behavior across input modes
- **WHEN** the same command sequence is supplied interactively and through a file from equivalent initial state
- **THEN** the output of each command and the resulting users are equivalent

### Requirement: Invalid commands are recoverable
An unknown command, malformed command text, or a command with insufficient parameters SHALL produce an error message without terminating the application, and processing SHALL continue with the next command.

#### Scenario: Malformed input in interactive mode
- **WHEN** malformed or unknown command text is entered in an interactive session
- **THEN** an error message is shown and the next command is processed normally

### Requirement: Invalid command file
If the command file does not exist, cannot be opened, or cannot be read, the application SHALL report the problem with an error message and terminate in a controlled manner without displaying an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the application is launched with a path to a file that does not exist
- **THEN** an error message naming the problem is shown, no stack trace is shown, and the application terminates

### Requirement: HELP command
The `HELP` command SHALL display, from the registry of available commands, every command with its basic syntax and no description: the F1 user commands, `SYSTEM DATE`, `SYSTEM PRICE`, the `GROUP`, `TICKET`, `GROUPTICKET`, `ATTRACTION`, `SPEEDPASS`, `SPECIALPASS`, and `INVOICE` commands, `HELP`, `HELP EXTENDED`, and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is processed
- **THEN** every command listed in this requirement is displayed with its syntax

#### Scenario: F3 commands listed
- **WHEN** `HELP` is processed
- **THEN** `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, and `HELP EXTENDED` appear with their syntax

### Requirement: EXIT command
The `EXIT` command SHALL terminate interactive mode in a controlled manner and SHALL stop file processing immediately so that no later command in the file is executed.

#### Scenario: No commands after EXIT in file mode
- **WHEN** a file contains a `USER CREATE` command, then `EXIT`, then another `USER CREATE` command
- **THEN** only the first creation is executed and the later one is not

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, from the same command registry, every command's syntax followed by a brief description of its behavior. The help text SHALL be produced by a help generator that receives its output format through dependency injection, so new formats can be added without changing command processing.

#### Scenario: Syntax and descriptions
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command shown by `HELP` is shown with its syntax and a description

#### Scenario: Same registry for both modes
- **WHEN** a command is registered
- **THEN** it appears in both `HELP` and `HELP EXTENDED` output without further changes

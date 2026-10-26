# Spec Delta

## MODIFIED Requirements

### Requirement: Terminal command input
When started without a command-file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input, one command per line, and SHALL print a response for every non-empty command, continuing to wait for commands until it receives `EXIT` or the input ends.

#### Scenario: Command processed from terminal
- **WHEN** the user types `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the application prints a success message and waits for the next command

#### Scenario: Input ends
- **WHEN** the terminal input reaches end of stream
- **THEN** the application terminates normally

#### Scenario: EXIT ends interactive mode
- **WHEN** the user types `HELP` and then `EXIT`
- **THEN** the help is shown, the application stops asking for commands and terminates normally

### Requirement: File command input
When started with an argument other than `--fly`, the application SHALL treat the first such argument as the path of a text file, read it line by line and execute each non-blank line as a command in file order, stopping at `EXIT` or at the end of the file.

#### Scenario: Commands executed in order
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` commands, a `USER UPDATE` of the first user's phone and `EXIT`
- **THEN** both users are created, the phone is updated, a response is printed for each command and the application terminates

#### Scenario: Blank lines in the file
- **WHEN** the file contains blank lines between commands
- **THEN** the blank lines are ignored and every command is processed without errors

### Requirement: Invalid command file
If the command file argument does not exist, cannot be opened or cannot be read, the application SHALL print an error message identifying the file and terminate without showing an unhandled Java exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with `no_existe.txt` and that file does not exist
- **THEN** an error message mentioning `no_existe.txt` is printed and the application terminates without a stack trace

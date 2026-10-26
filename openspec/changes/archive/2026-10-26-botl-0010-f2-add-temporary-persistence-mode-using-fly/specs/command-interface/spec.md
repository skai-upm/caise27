# Spec Delta

## MODIFIED Requirements

### Requirement: Terminal input
When started without a command file argument, the system SHALL read commands line by line from standard input and process each line in order until it receives `EXIT` or the input ends.

#### Scenario: Several commands from the terminal
- **WHEN** two valid command lines are entered and standard input then ends
- **THEN** both commands are processed in order and the application terminates normally

#### Scenario: EXIT ends interactive mode
- **WHEN** the lines `USER CREATE;...`, `HELP` and `EXIT` are entered
- **THEN** the system processes the first two, stops asking for commands after `EXIT` and terminates

#### Scenario: Only options supplied
- **WHEN** the application is started with `--fly` as its only argument
- **THEN** it reads commands from standard input

### Requirement: Command file input
When an execution argument that is not an option (does not start with `--`) is supplied, the system SHALL treat the first such argument as the path of a text file, read it line by line and execute each line as a command in the order it appears.

#### Scenario: Commands executed from a file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** both users are created, the update is applied and the application terminates

#### Scenario: File after an option
- **WHEN** the application is started with `--fly input_basico.txt`
- **THEN** `input_basico.txt` is used as the command file

## ADDED Requirements

### Requirement: Unknown options are rejected
An argument starting with `--` that is not a supported option SHALL be reported as an error and the application SHALL terminate without processing commands.

#### Scenario: Unknown option
- **WHEN** the application is started with `--fast`
- **THEN** an `ERROR` naming the option is printed and no command is processed

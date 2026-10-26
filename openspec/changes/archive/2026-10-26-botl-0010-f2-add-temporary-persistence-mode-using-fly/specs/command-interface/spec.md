# Spec Delta

## MODIFIED Requirements

### Requirement: Execution mode selection
The execution mode SHALL depend on the program arguments. The argument `--fly`, if present, selects temporary persistence and is not a file path. Of the remaining arguments, if there are none the application SHALL use interactive mode on standard input; otherwise the first one SHALL be interpreted as the path of a command file.

#### Scenario: No arguments
- **WHEN** the application is started without arguments
- **THEN** it reads commands from standard input

#### Scenario: File argument
- **WHEN** the application is started with the argument `input_basico.txt`
- **THEN** it reads commands from that file and not from standard input

#### Scenario: Fly option alone
- **WHEN** the application is started with `--fly`
- **THEN** it reads commands from standard input using temporary persistence

#### Scenario: Fly option with file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** it reads commands from `input_fase2.txt` using temporary persistence

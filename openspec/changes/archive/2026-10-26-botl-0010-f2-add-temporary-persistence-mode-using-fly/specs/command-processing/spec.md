# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When started without a command file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input and keep waiting for new commands until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and the user types `USER CREATE;...`, `HELP` and `EXIT`
- **THEN** each command is executed in order and the application stops asking for commands after `EXIT`

### Requirement: File mode
When started with a command file argument, the application SHALL interpret the first argument that is not `--fly` as the path of a command file, read it line by line and execute each non-blank line in order with the same command processing used in interactive mode.

#### Scenario: Commands from a file
- **WHEN** the application is started with `input_basico.txt` containing two `USER CREATE` lines, a `USER UPDATE` line and `EXIT`
- **THEN** the two users are created, the update is applied, and the application terminates

#### Scenario: Same result in both modes
- **WHEN** the same command is supplied once through the terminal and once through a file
- **THEN** it produces the same response and the same effect

#### Scenario: File after --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is used as the command file

# Spec Delta

## MODIFIED Requirements

### Requirement: Execution mode selection
When started without arguments, the application SHALL run in interactive mode and read commands from standard input. When started with at least one argument, it SHALL treat the first argument as the path of a command file and run in file mode. As an exception, when the first argument is exactly `--fly`, the application SHALL run in temporary persistence mode, and the optional second argument SHALL be the command file; without it, interactive mode is used.

#### Scenario: No arguments selects interactive mode
- **WHEN** the application is started with no arguments
- **THEN** it waits for commands on standard input

#### Scenario: One argument selects file mode
- **WHEN** the application is started as `java -jar target/amusement-park.jar input.txt`
- **THEN** it processes the commands contained in `input.txt`

#### Scenario: Temporary mode, interactive
- **WHEN** the application is started as `java -jar target/amusement-park.jar --fly`
- **THEN** it runs in temporary persistence mode and waits for commands on standard input

#### Scenario: Temporary mode with a command file
- **WHEN** the application is started as `java -jar target/amusement-park.jar --fly input_fase2.txt`
- **THEN** it runs in temporary persistence mode and processes the commands contained in `input_fase2.txt`

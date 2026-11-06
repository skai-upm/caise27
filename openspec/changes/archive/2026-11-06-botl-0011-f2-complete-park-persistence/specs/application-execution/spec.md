# Spec Delta

## MODIFIED Requirements

### Requirement: File mode
The application SHALL, when started with a command file argument, treat it as the path to a text file with one command per line and execute the commands in the order in which they appear in the file. The command file argument is the first argument, or the second when the first is `--fly`.

#### Scenario: Commands from a file run in order
- **GIVEN** a file `input.txt` containing `FOO`, `HELP` and `EXIT` on separate lines
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** the unknown-command error for `FOO` is displayed before the help text
- **AND** the application terminates after `EXIT`

#### Scenario: Nonexistent command file
- **GIVEN** a path that does not exist or cannot be read
- **WHEN** the application is started with that path as its argument
- **THEN** an error message stating that the command file cannot be read is displayed
- **AND** the application terminates without a stack trace and without executing any command

#### Scenario: Command file after --fly
- **GIVEN** a file `input.txt` containing `HELP` and `EXIT`
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` is run
- **THEN** the commands of `input.txt` are executed in order

## ADDED Requirements

### Requirement: Supported execution forms
The application SHALL support exactly these argument forms: no arguments (interactive), `<file>` (file mode), `--fly` (interactive, temporary persistence) and `--fly <file>` (file mode, temporary persistence). Without `--fly`, argument behavior SHALL be the same as in Delivery 1.

#### Scenario: Interactive fly mode
- **WHEN** `java -jar target/amusement-park.jar --fly` is run with `HELP` and `EXIT` on standard input
- **THEN** the help text is displayed and the application terminates normally

# Spec Delta

## ADDED Requirements

### Requirement: Supported argument forms
The application SHALL accept exactly these forms: no arguments (interactive), `<file>` (file mode), `--fly` (interactive, temporary persistence) and `--fly <file>` (file mode, temporary persistence). Without `--fly`, argument handling SHALL be the same as in the previous delivery.

#### Scenario: Fly with a command file
- **WHEN** the user runs `java -jar target/amusement-park.jar --fly input.txt`
- **THEN** the commands of `input.txt` are processed in temporary persistence mode

#### Scenario: Fly interactive
- **WHEN** the user runs `java -jar target/amusement-park.jar --fly` and types commands
- **THEN** the commands are read from standard input in temporary persistence mode

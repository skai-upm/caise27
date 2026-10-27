# Spec Delta

## ADDED Requirements

### Requirement: Supported launch forms
The application SHALL support exactly these launch forms: no arguments (interactive), `<file>` (file mode), `--fly` (interactive, temporary persistence) and `--fly <file>` (file mode, temporary persistence); without `--fly` argument behaviour SHALL remain as in Delivery 1.

#### Scenario: Fly with file
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input.txt` is executed
- **THEN** the commands of `input.txt` are processed using temporary persistence

#### Scenario: Fly interactive
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly` is executed
- **THEN** commands are read from standard input using temporary persistence

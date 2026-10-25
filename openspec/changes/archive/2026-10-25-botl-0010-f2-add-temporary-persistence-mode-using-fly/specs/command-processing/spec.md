# Spec Delta

## MODIFIED Requirements

### Requirement: Execution mode selection
The application SHALL run in interactive mode, reading standard input, when started without arguments, and in file mode, reading the file whose path is the first argument, when started with at least one argument. The option `--fly` (temporary persistence) is not a file path: it is removed from the arguments before this selection, so `--fly` alone means interactive mode and `--fly <file>` means file mode.

#### Scenario: No arguments
- **WHEN** the application is started without arguments and receives `HELP` and `EXIT` on standard input
- **THEN** it prints the help and terminates

#### Scenario: File argument
- **WHEN** the application is started with argument `input_basico.txt`
- **THEN** it processes the commands contained in that file instead of standard input

#### Scenario: Fly option is not a file
- **WHEN** the application is started with the single argument `--fly`
- **THEN** it runs in interactive mode with temporary persistence

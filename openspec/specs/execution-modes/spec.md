# execution-modes Specification

## Purpose

Defines how the application obtains the commands it processes when launched: interactively from standard input, or from a text file given as the single argument.

## Requirements

### Requirement: Interactive mode
When launched without arguments, the application SHALL start in interactive mode and read commands from standard input; after processing each command it SHALL continue waiting for the next one until the input ends.

#### Scenario: Started without arguments
- **WHEN** `java -jar target/nombre-aplicacion.jar` is executed without arguments
- **THEN** the application reads lines from standard input and does not terminate until standard input is closed

#### Scenario: Continues after each command
- **WHEN** two valid USER CREATE commands for different users are typed one after the other
- **THEN** both are executed and each prints `USER CREATED`

### Requirement: File input mode
When launched with a text file path as first argument, the application SHALL process the commands contained in that file, one command per line, in the order in which they appear, with the same processing and output as interactive mode.

#### Scenario: Started with an input file
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is executed and `input.txt` exists
- **THEN** the application reads the lines of `input.txt` as its command input and terminates after the last line

#### Scenario: Commands run in file order
- **WHEN** the file contains a USER CREATE for Ana, then a USER UPDATE of Ana's phone, then a USER DELETE of Ana
- **THEN** the outputs are `USER CREATED`, `USER UPDATED`, `USER DELETED` in that order

#### Scenario: Same behaviour as interactive mode
- **WHEN** the same sequence of lines is given once through standard input and once as a file (each run starting from the same stored data)
- **THEN** both runs produce identical standard output

#### Scenario: Input file cannot be read
- **WHEN** the application is launched with a path that does not exist or cannot be read
- **THEN** it writes an error message to standard error and exits with a non-zero status

### Requirement: Blank lines ignored
Empty or whitespace-only input lines SHALL be ignored in every input mode, producing no output.

#### Scenario: Blank lines between commands
- **WHEN** the input contains an empty line and a line of spaces between two commands
- **THEN** only the two commands' responses are printed

### Requirement: Supported launch forms
The application SHALL support exactly these launch forms: no arguments (interactive), `<file>` (file mode), `--fly` (interactive, temporary persistence) and `--fly <file>` (file mode, temporary persistence); without `--fly` argument behaviour SHALL remain as in Delivery 1.

#### Scenario: Fly with file
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input.txt` is executed
- **THEN** the commands of `input.txt` are processed using temporary persistence

#### Scenario: Fly interactive
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly` is executed
- **THEN** commands are read from standard input using temporary persistence

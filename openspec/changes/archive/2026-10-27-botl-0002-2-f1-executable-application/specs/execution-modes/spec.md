# Spec Delta

## Purpose

Defines how the application obtains the commands it processes when launched: interactively from standard input, or from a text file given as the single argument.

## ADDED Requirements

### Requirement: Interactive mode
When launched without arguments, the application SHALL start in interactive mode and wait for commands from standard input until the input ends.

#### Scenario: Started without arguments
- **WHEN** `java -jar target/nombre-aplicacion.jar` is executed without arguments
- **THEN** the application reads lines from standard input and does not terminate until standard input is closed

### Requirement: File input mode
When launched with a text file path as argument, the application SHALL process the commands contained in that file.

#### Scenario: Started with an input file
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` is executed and `input.txt` exists
- **THEN** the application reads the lines of `input.txt` as its command input and terminates after the last line

#### Scenario: Input file cannot be read
- **WHEN** the application is launched with a path that does not exist or cannot be read
- **THEN** it writes an error message to standard error and exits with a non-zero status

# command-processing Specification

## Purpose

Defines how the application interprets text commands: elements separated by `;`, parsed with basic String operations, with controlled handling of input that cannot be processed.

## Requirements

### Requirement: Semicolon-separated commands
Each input line SHALL be interpreted as one command whose elements are separated by `;`, the first element naming the command; parsing SHALL use basic String operations including split, without external libraries or regular expressions.

#### Scenario: Command recognised by its first element
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** it is handled as the `USER CREATE` command with the remaining seven elements as its arguments

### Requirement: Unknown commands handled safely
A line whose command is not recognised SHALL produce an error message and the application SHALL continue processing subsequent commands.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is processed
- **THEN** an error message indicating an unknown command is written and the next line is still processed

### Requirement: Same processing for every input source
Commands SHALL be executed by a single processing component regardless of whether they come from standard input or from a file, and responses SHALL be written to standard output.

#### Scenario: Response printed in file mode
- **WHEN** the application runs with a file containing a valid `USER CREATE` line
- **THEN** `USER CREATED` is written to standard output, exactly as in interactive mode

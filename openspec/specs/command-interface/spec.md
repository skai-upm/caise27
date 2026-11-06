# command-interface Specification

## Purpose

Defines how the Amusement Park application receives text commands, splits them into fields with `;`, dispatches them to the matching operation, and handles incorrect commands without terminating.

## Requirements

### Requirement: Interactive command input
When started without arguments, after printing its start banner the application SHALL read commands from standard input, one command per line, process each line in order and print the resulting message, waiting for the next command until standard input ends. Blank lines MUST be ignored.

#### Scenario: Commands processed in order
- **GIVEN** the application is started with two valid `USER CREATE` lines on standard input
- **WHEN** the input is processed
- **THEN** one result message is printed for each line, in input order

#### Scenario: End of input stops reading
- **GIVEN** the application is started with empty standard input
- **WHEN** it runs
- **THEN** it prints the start banner and finishes normally

### Requirement: Semicolon-separated command format
A command line SHALL consist of the command name followed by its fields, all separated by the `;` character, and MUST be interpreted with basic String operations including `split`, without external libraries. The command name is matched exactly, ignoring surrounding whitespace.

#### Scenario: Command name and fields are separated
- **GIVEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** it is interpreted
- **THEN** the command `USER CREATE` is executed with the seven fields `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`

### Requirement: Incorrect commands do not terminate the application
An unknown command, a command with the wrong number of fields, or a line whose processing fails unexpectedly SHALL produce an error message and MUST NOT terminate the application nor change any stored data; processing continues with the next line.

#### Scenario: Unknown command
- **GIVEN** the line `FOO BAR;1`
- **WHEN** it is processed
- **THEN** an error message reporting an unknown command is printed, no user is added, and the next line is still processed

#### Scenario: Wrong number of fields
- **GIVEN** the line `USER CREATE;Ana;Garcia Lopez`
- **WHEN** it is processed
- **THEN** an error message reporting the wrong number of fields is printed and no user is added

#### Scenario: Unexpected processing failure
- **GIVEN** a line whose processing raises an unexpected internal error
- **WHEN** it is processed
- **THEN** an error message is printed and the next line is still processed

### Requirement: Command file input
When started with a first argument, the application SHALL treat it as the path of a text file with one command per line, process the commands in the order they appear, ignore blank lines and not read standard input.

#### Scenario: Commands processed in file order
- **GIVEN** a file with the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, an empty line, and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`
- **WHEN** `java -jar target/amusement-park.jar <file>` is run
- **THEN** the creation result is printed before the update result, and the stored user's phone is `699999999`

### Requirement: Same behavior for every input source
A command SHALL be processed by the same command processing and produce the same output and effects whether it comes from standard input or from a command file.

#### Scenario: Identical output
- **GIVEN** the same sequence of command lines
- **WHEN** it is supplied once on standard input and once as a command file, each time starting from the same stored users
- **THEN** the printed results and the resulting stored users are identical

### Requirement: Unreadable command file handled
If the command file does not exist or cannot be read, the application SHALL print an error message naming the path and finish normally, without an uncontrolled termination or stack trace and without changing stored data.

#### Scenario: Nonexistent file
- **WHEN** the application is started with the argument `missing.txt` and no such file exists
- **THEN** an error message naming `missing.txt` is printed, no stack trace is shown, and the process exits normally

#### Scenario: Path is not a readable file
- **WHEN** the application is started with a directory path as argument
- **THEN** an error message naming that path is printed and the process exits normally

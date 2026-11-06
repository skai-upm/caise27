# Spec Delta

## MODIFIED Requirements

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

## ADDED Requirements

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

## ADDED Requirements

### Requirement: File mode
When launched with a file path as its first argument (`java -jar target/amusement-park.jar input.txt`), the application SHALL read the file as one command per line, process the commands in the order in which they appear, ignore blank lines, and terminate normally after the last line.

#### Scenario: Commands processed in file order
- **GIVEN** `input.txt` containing a `USER CREATE` for Ana, a blank line, and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`
- **WHEN** `java -jar target/amusement-park.jar input.txt` is run
- **THEN** `USER CREATED` then `USER UPDATED` are printed in that order and the application exits with status 0

#### Scenario: Errors in the file do not stop processing
- **GIVEN** a file whose first line is an unknown command and whose second line is a valid `USER CREATE`
- **WHEN** the file is processed
- **THEN** an `ERROR` message is printed for the first line and `USER CREATED` for the second

### Requirement: Identical processing for both input sources
Command processing SHALL be identical whether commands come from standard input or from a file: the same command in the same state SHALL produce the same effect and response.

#### Scenario: Same response from file and interactive input
- **GIVEN** the same sequence of commands supplied once on standard input and once as a file, each starting from an empty users file
- **WHEN** both runs complete
- **THEN** both print the same responses and leave the same stored users

### Requirement: Controlled handling of unusable command file
If the file given as argument does not exist or cannot be read, the application SHALL print an error message starting with `ERROR` that identifies the file and terminate in a controlled manner with exit status 1, without an uncaught exception or stack trace and without processing any command.

#### Scenario: Nonexistent file
- **WHEN** `java -jar target/amusement-park.jar missing.txt` is run and `missing.txt` does not exist
- **THEN** an `ERROR` message naming `missing.txt` is printed, no stack trace is shown and the exit status is 1

#### Scenario: Unreadable file
- **GIVEN** a path that cannot be read as a file (for example a directory)
- **WHEN** the application is launched with that path
- **THEN** an `ERROR` message is printed and the application terminates with exit status 1

# Spec Delta

## ADDED Requirements

### Requirement: Command file processing order
When a command file is given as the first argument, the application SHALL process its commands one per line in the order in which they appear, ignoring blank lines, using the same command processing as interactive mode so that each command's observable behaviour is identical in both modes.

#### Scenario: Source example processed in order
- **WHEN** a file contains `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`, one per line
- **THEN** the output shows `USER CREATED`, `USER CREATED`, `USER UPDATED` in that order and the application ends

#### Scenario: Blank lines ignored
- **WHEN** the command file contains blank lines between commands
- **THEN** the blank lines produce no output and do not affect the other commands

#### Scenario: Same output in both modes
- **WHEN** the same sequence of commands is given once on standard input and once as a command file, each starting from the same stored data
- **THEN** both executions print the same output

#### Scenario: Interactive mode keeps waiting
- **WHEN** a command is entered in interactive mode
- **THEN** after processing it the application waits for the next command until `EXIT` or end of input

### Requirement: Controlled handling of command file errors
A nonexistent or unreadable command file SHALL be handled in a controlled manner: the application SHALL print an error message naming the file, SHALL NOT process any command and SHALL end normally without an unhandled exception.

#### Scenario: Nonexistent file
- **WHEN** the application is started with `missing.txt` and that file does not exist
- **THEN** an error stating that the command file was not found is printed and the application ends normally

#### Scenario: Unreadable file
- **WHEN** the application is started with a path that exists but cannot be read as a file (for example a directory)
- **THEN** an error stating that the command file cannot be read is printed and the application ends normally

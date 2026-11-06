# Spec Delta

## ADDED Requirements

### Requirement: Command file input
When the application is started with the path of a text file as its first argument (or as the argument following `--fly`), it SHALL read one command per line from that file and execute the commands in the order they appear, ignoring blank lines. It SHALL use the same command processing as interactive mode, so each command's observable behavior is identical whatever its source. A nonexistent or unreadable file SHALL produce an error message and a normal end, without a stack trace.

#### Scenario: Same behavior from file and standard input
- **GIVEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `USER DELETE;ana@correo.com;Clave1!`, `HELP` and `EXIT`
- **WHEN** they are processed once from standard input and once from a command file, each starting from empty data
- **THEN** both outputs are identical

#### Scenario: File order preserved
- **GIVEN** a file whose lines create group `G01`, then delete it, then create `G01` again
- **WHEN** the file is processed
- **THEN** the outputs appear in that order and `G01` exists at the end

#### Scenario: Unreadable command file
- **WHEN** the application is started with a nonexistent path, or with the path of a directory
- **THEN** `ERROR: Cannot read command file: <path>` is printed and the application ends normally

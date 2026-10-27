# Spec Delta

## ADDED Requirements

### Requirement: Unexpected errors are contained
Any unexpected error raised while processing a single command SHALL be reported as an error message, and the application SHALL continue with the next command instead of terminating.

#### Scenario: Internal failure during a command
- **WHEN** processing a command fails with an unexpected runtime error
- **THEN** an error message is printed and the following command is still processed

### Requirement: Delivery 1 example flow
The application SHALL execute the assignment's basic example — creating Ana and Pedro, updating Ana's phone and email, deleting Pedro, HELP and EXIT — with the same results interactively and from a command file.

#### Scenario: Example from a command file
- **WHEN** a file with `USER CREATE` (Ana), `USER CREATE` (Pedro), `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, `USER DELETE;pedro@correo.com;Otra2#`, `HELP` and `EXIT` is processed
- **THEN** the output shows `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text, and the application ends with only Ana (email `ana.nueva@correo.com`, phone `699999999`) stored

#### Scenario: Example interactively
- **WHEN** the same commands are typed on standard input from the same starting data
- **THEN** the output and stored users are identical to the file execution

# Spec Delta

## MODIFIED Requirements

### Requirement: Incorrect commands do not terminate the application
An unrecognized, incomplete or otherwise incorrect command SHALL produce an error response starting with `ERROR` and MUST NOT terminate the application or change any stored data; subsequent commands SHALL still be processed.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is entered, followed by a valid `USER CREATE` command
- **THEN** an error response starting with `ERROR` is printed for `FOO;bar`
- **AND** the following `USER CREATE` is still processed and the user is created

#### Scenario: Not-yet-supported command
- **WHEN** a line whose command name is not implemented in this delivery step (e.g. `USER UPDATE;ana@correo.com;Clave1!`) is entered
- **THEN** an error response starting with `ERROR` is printed and the application keeps running

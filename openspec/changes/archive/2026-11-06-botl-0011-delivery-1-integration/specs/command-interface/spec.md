# Spec Delta

## MODIFIED Requirements

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

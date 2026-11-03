# Spec Delta

## MODIFIED Requirements

### Requirement: Text command interpretation
The application SHALL read commands as text lines from the active input source (the terminal, or the command file given as first launch argument), split them into components with `String.split` on `;` (the first component holding the operation words separated by a space), and SHALL report invalid operations or wrong parameter counts as errors without terminating.

#### Scenario: Unknown operation
- **WHEN** `USER RENAME;ana@correo.com` is processed
- **THEN** an error is reported, no user changes, and the application keeps accepting commands

#### Scenario: Insufficient parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** an error is reported and no user is created

#### Scenario: Same interpretation from a file
- **GIVEN** a command file containing `USER CREATE;Ana;Garcia Lopez`
- **WHEN** the application runs with that file
- **THEN** the same error is reported as when the line is typed in the terminal and no user is created

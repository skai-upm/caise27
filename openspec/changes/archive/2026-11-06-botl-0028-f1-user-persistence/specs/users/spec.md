# Spec Delta

## ADDED Requirements

### Requirement: Users persist between executions
The system SHALL store the users in a text file and load them when the application starts, before processing any command. Every successful user operation SHALL be stored before the next command runs. Reading and writing the file SHALL be done by a component separate from the user class and from the main class, without databases, persistence frameworks or external persistence libraries.

#### Scenario: Users recovered after restart
- **GIVEN** an execution that created users `ana@correo.com` and `pedro@correo.com` and then ended
- **WHEN** the application starts again with the same data directory
- **THEN** both users exist with all their data, and creating `ana@correo.com` again is rejected as a duplicate

### Requirement: User file format
Each stored user SHALL occupy one line with its attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card. The password field SHALL contain the protected value, never the original password. A line that cannot be interpreted SHALL be reported and skipped.

#### Scenario: Line mapping
- **GIVEN** the stored line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<protected>;612345678;1234567890123456`
- **WHEN** the application loads it
- **THEN** a user exists with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, phone `612345678` and card `1234567890123456`, whose password verifies against the original one

#### Scenario: No plain-text password in the file
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file does not contain `Clave1!`

#### Scenario: Malformed line
- **GIVEN** the users file contains a line with fewer than seven fields
- **WHEN** the application starts
- **THEN** a warning is shown and the other users are loaded

### Requirement: Temporary mode isolates users
In `--fly` mode, users SHALL be stored only in the temporary directory, through the same repository as in normal mode, and SHALL disappear with it at the end of the execution.

#### Scenario: Fly run does not touch normal users
- **GIVEN** normal data with user `ana@correo.com`
- **WHEN** a `--fly` run creates `pedro@correo.com`
- **THEN** during that run `ana@correo.com` does not exist, and afterwards the normal users file still contains only `ana@correo.com`

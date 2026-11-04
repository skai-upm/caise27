## MODIFIED Requirements

### Requirement: Command format and responses
The system SHALL read commands as text lines, split them on `;` into an operation followed by parameters, and answer every command with a response whose first line starts with `OK` on success or `ERROR:` on rejection. Success responses SHALL start with `OK:` unless the source of a command prescribes an exact text (`TICKET CREATE` answers `OK TICKET CREATED -> <Precio>`). Responses to `USER` commands SHALL consist of exactly one line; general commands such as `HELP` MAY add further lines.

#### Scenario: Success response
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** a single line starting with `OK:` is printed

#### Scenario: Rejection response
- **WHEN** any command is rejected
- **THEN** a single line starting with `ERROR:` explaining the reason is printed

#### Scenario: Multi-line general response
- **WHEN** `HELP` is processed
- **THEN** the first printed line starts with `OK:` and the command list follows on the next lines

#### Scenario: Prescribed success text
- **WHEN** a valid `TICKET CREATE` is processed at price 40.00
- **THEN** the single printed line is `OK TICKET CREATED -> 40.00`

### Requirement: Delete user
The system SHALL process `USER DELETE;Correo;Contraseña`, deleting the user only if a user with that email exists, the password matches the stored one, and the user owns no ticket dated on or after the system date. A deleted user SHALL also be removed from the members of every group.

#### Scenario: Successful deletion
- **GIVEN** an existing user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is removed and an `OK:` response is printed

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` response is printed and no user is removed

#### Scenario: Deleted user leaves their groups
- **GIVEN** Ana is a member of `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `G01` still exists and no longer contains Ana, also after a restart

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and Ana owns a ticket dated 2026-10-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` response is printed, Ana is still registered and her group memberships are unchanged

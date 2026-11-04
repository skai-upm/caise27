# user-management Specification

## Purpose
Defines the text commands that create, delete and update users, their authentication by email and password, uniqueness rules and the safe handling of invalid commands.

## Requirements

### Requirement: Command format and responses
The system SHALL read commands as text lines, split them on `;` into an operation followed by parameters, and answer every command with a response whose first line starts with `OK` on success or `ERROR:` on rejection. Success responses SHALL start with `OK:` unless the source of a command prescribes an exact text (`TICKET CREATE` answers `OK TICKET CREATED -> <Precio>` and `GROUPTICKET CREATE` answers `OK GROUP TICKET CREATED -> <Precio>`). Responses to `USER` commands SHALL consist of exactly one line; general commands such as `HELP` MAY add further lines.

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

#### Scenario: Prescribed group ticket text
- **WHEN** a group of 2 buys a group ticket at price 40.00
- **THEN** the single printed line is `OK GROUP TICKET CREATED -> 80.00`

### Requirement: Invalid commands do not terminate the application
The system SHALL reject unknown operations, empty input, and commands with fewer or more parameters than their format defines, without terminating and without changing any stored data.

#### Scenario: Unknown operation
- **WHEN** `USER RENAME;ana@correo.com` or `HELLO` is processed
- **THEN** an `ERROR:` response is printed and the application keeps accepting commands

#### Scenario: Missing parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an `ERROR:` response is printed and no user is created

### Requirement: Create user
The system SHALL process `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, validating every field before storing anything, and SHALL create the user only if all fields are valid.

#### Scenario: Valid creation
- **GIVEN** no user with DNI `12345678Z` or email `ana@correo.com`
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is created and an `OK:` response is printed

#### Scenario: Any invalid field prevents creation
- **WHEN** a `USER CREATE` contains an invalid DNI, email, password or card number
- **THEN** an `ERROR:` response is printed and no user is stored

### Requirement: Unique DNI and email
The system SHALL reject creation of a user whose DNI or email already belongs to an existing user.

#### Scenario: Duplicate DNI
- **GIVEN** an existing user `12345678Z;ana@correo.com`
- **WHEN** a user with DNI `12345678Z` and email `otro@correo.com` is created
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** an existing user `12345678Z;ana@correo.com`
- **WHEN** a user with DNI `87654321X` and email `ana@correo.com` is created
- **THEN** creation is rejected

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

### Requirement: Update user
The system SHALL process `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, authenticating with the current email and password, revalidating the new value under its field's rules, and applying the change only if all checks pass.

#### Scenario: Successful updates chain
- **GIVEN** an existing user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria`, then `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** each command succeeds and the user has name `Ana Maria`, email `ana.nueva@correo.com`, password `Nueva2#` and card `9999888877776666`

#### Scenario: Old credentials stop working
- **GIVEN** the email of a user was changed from `ana@correo.com` to `ana.nueva@correo.com`
- **WHEN** an update or delete authenticates with `ana@correo.com`
- **THEN** it is rejected

#### Scenario: Failed authentication
- **WHEN** an update supplies a non-existent email or a wrong password
- **THEN** an `ERROR:` response is printed and the user is unchanged

#### Scenario: Invalid new value
- **WHEN** an update sets `EMAIL` to an email used by another user or to `ana@correo.es`, `PASSWORD` to `clave1!`, or `CARD` to `1234`
- **THEN** an `ERROR:` response is printed and the previous value is kept

#### Scenario: Keeping one's own email
- **WHEN** a user updates `EMAIL` to the email they already have
- **THEN** the update is accepted

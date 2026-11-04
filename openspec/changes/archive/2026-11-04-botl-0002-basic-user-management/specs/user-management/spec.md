# Spec Delta

## Purpose

Defines the text-command interface for creating, deleting and updating users, including uniqueness, authentication and safe handling of invalid commands.

## ADDED Requirements

### Requirement: Command parsing
The system SHALL interpret each command line by splitting it with `String.split` on `;`, where the first element names the operation (`USER CREATE`, `USER DELETE` or `USER UPDATE`) and the remaining elements are its parameters. Each command SHALL produce one result message beginning with `OK` on success or `ERROR` on failure.

#### Scenario: Unknown operation
- **WHEN** `USER RENAME;ana@correo.com` is processed
- **THEN** an `ERROR` message is produced, no data changes, and the application keeps accepting commands

#### Scenario: Wrong parameter count
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an `ERROR` message is produced and no user is created

#### Scenario: Empty or garbage line
- **WHEN** an empty line or `hello` is processed
- **THEN** an `ERROR` message is produced and the application does not terminate

### Requirement: User creation
The system SHALL process `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, validating every value before storing anything; if any value is invalid the user SHALL NOT be stored.

#### Scenario: Valid creation
- **GIVEN** no existing users
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an `OK` message is produced and the user is stored and persisted

#### Scenario: Invalid data rejected atomically
- **WHEN** a creation command contains DNI `12345678A`, email `ana@correo.es`, password `Clave12` or card `123456789012345`
- **THEN** an `ERROR` message names the invalid field and no user is stored or persisted

### Requirement: User uniqueness
The system SHALL reject creation of a user whose DNI or email already belongs to an existing user. Email comparison SHALL be case-insensitive.

#### Scenario: Duplicate DNI
- **GIVEN** an existing user `12345678Z;ana@correo.com`
- **WHEN** a user with `12345678Z;otro@correo.com` is created
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** an existing user `12345678Z;ana@correo.com`
- **WHEN** a user with `87654321X;ana@correo.com` is created
- **THEN** creation is rejected

### Requirement: Authenticated deletion
The system SHALL process `USER DELETE;Correo;Contraseña` by locating the user by email, verifying the password matches the stored password, removing the user and updating the persistent file.

#### Scenario: Successful deletion
- **GIVEN** an existing user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `OK` message is produced and the user no longer exists in memory or in the file

#### Scenario: Wrong password or unknown email
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an `ERROR` message is produced and no user is deleted

### Requirement: Authenticated update
The system SHALL process `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. It SHALL verify the current email and password first, revalidate the new value with the rules for that field, and persist the user's new state on success.

#### Scenario: Update first name
- **GIVEN** an existing user `ana@correo.com` / `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the stored first name is `Ana Maria`

#### Scenario: Update email, then password, then card
- **GIVEN** an existing user `ana@correo.com` / `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** all three succeed and the persisted user has email `ana.nueva@correo.com`, password `Nueva2#` and card `9999888877776666`

#### Scenario: Update surname and phone
- **WHEN** valid `SURNAME` and `PHONE` updates are processed with correct credentials
- **THEN** the stored values change accordingly

#### Scenario: Rejected update leaves data unchanged
- **GIVEN** existing users `ana@correo.com` and `pedro@correo.com`
- **WHEN** Ana's update uses a wrong current password, sets `EMAIL` to `pedro@correo.com`, sets `PASSWORD` to `clave1!`, or sets `CARD` to `1234`
- **THEN** an `ERROR` message is produced and Ana's stored data is unchanged

#### Scenario: Unsupported field
- **WHEN** an update names a field outside the enum, such as `DNI`
- **THEN** an `ERROR` message is produced and no data changes

### Requirement: Terminal interaction
The application SHALL read commands line by line from standard input, print each command's result message, and continue until the input ends.

#### Scenario: Several commands in one session
- **WHEN** a create command followed by an invalid command and a delete command are entered
- **THEN** three result lines are printed and the session ends normally at end of input

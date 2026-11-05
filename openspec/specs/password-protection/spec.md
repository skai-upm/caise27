# password-protection Specification

## Purpose
Defines how user passwords are protected: they are validated in plain text, stored only as BCrypt-protected values produced by the jBCrypt library, and verified against the stored value whenever an operation requires the user's password.

## Requirements

### Requirement: Passwords stored only as BCrypt values
When a user is created, the system SHALL validate the entered password with the Delivery 1 password rules and, only if it is valid, SHALL store a BCrypt-protected value of it generated with the jBCrypt library; the original password SHALL NOT be kept in memory in the user nor written to the users file.

#### Scenario: Created user's password is protected in the users file
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the line of `12345678Z` in `users.txt` does not contain `Clave1!`
- **AND** its password field is a BCrypt value (starting with `$2a$`) that jBCrypt verifies against `Clave1!`

#### Scenario: Invalid password rejected before protection
- **WHEN** `USER CREATE` is processed with password `clave1!`
- **THEN** an `ERROR` response is printed and no user is stored

### Requirement: Password verification against the protected value
Operations that require the user's password (`USER DELETE` and `USER UPDATE`) SHALL verify the entered password against the stored BCrypt value using the jBCrypt library; a password that does not match, or a stored value that is not a valid BCrypt value, SHALL be treated as an incorrect password.

#### Scenario: Correct password authenticates
- **GIVEN** a user created with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** a response starting with `OK` is printed

#### Scenario: Wrong password rejected
- **GIVEN** a user created with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave2!` is processed
- **THEN** an `ERROR` response reports an incorrect password and the user still exists

#### Scenario: The protected value itself is not accepted as password
- **GIVEN** a user created with password `Clave1!`
- **WHEN** `USER DELETE` is processed with the stored BCrypt value as password
- **THEN** an `ERROR` response is printed

#### Scenario: Verification after restart
- **GIVEN** a user created with password `Clave1!` and the application restarted
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** a response starting with `OK` is printed

### Requirement: Changed passwords are protected again
When `USER UPDATE` changes the `PASSWORD` field, the system SHALL validate the new password with the creation rules and SHALL generate and persist a new BCrypt value for it; afterwards only the new password SHALL authenticate the user.

#### Scenario: New password replaces the old one
- **GIVEN** a user created with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the users file contains a new BCrypt value that verifies `Nueva2#` and not `Clave1!`
- **AND** `USER DELETE;ana@correo.com;Clave1!` is rejected while `USER DELETE;ana@correo.com;Nueva2#` succeeds

#### Scenario: Invalid new password rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;sinmayus1!` is processed
- **THEN** an `ERROR` response is printed and the stored protected value is unchanged

### Requirement: Password protection behaviour is injected
The password protection behaviour SHALL be provided to user management as a replaceable dependency configured during application initialization (the jBCrypt-based implementation in normal execution). Users files written by earlier versions with plain-text passwords are not migrated.

#### Scenario: User management uses the configured protection
- **WHEN** the application is initialized
- **THEN** user creation, update and authentication use the configured jBCrypt-based password protection

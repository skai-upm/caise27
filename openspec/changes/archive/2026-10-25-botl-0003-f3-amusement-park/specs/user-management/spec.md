## ADDED Requirements

### Requirement: Password hashing
Passwords SHALL be validated with the password rules and then stored only as BCrypt hashes, never in plain text. `USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored hash. Updating `PASSWORD` SHALL store a new hash of the new password. Stored values that are not BCrypt hashes (legacy plain text) SHALL be replaced by their hash when users are loaded.

#### Scenario: Hash stored
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is executed
- **THEN** the stored password is a BCrypt hash that verifies `Clave1!` and is not the text `Clave1!`

#### Scenario: Verification on delete and update
- **WHEN** `USER DELETE` or `USER UPDATE` is executed with the correct password
- **THEN** the operation proceeds, and with a wrong password it is rejected

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is executed
- **THEN** a new hash verifying `Nueva2#` is persisted and `Clave1!` no longer authenticates

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected before any hash is generated

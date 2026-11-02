# Spec Delta

## ADDED Requirements

### Requirement: Password protection
Passwords SHALL be validated with the E1 password rules and then stored only as BCrypt hashes. `USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored hash, and a password change SHALL store a new BCrypt hash of the new password.

#### Scenario: No plaintext stored
- **WHEN** a user is created with password `Clave1!`
- **THEN** the stored password value is a BCrypt hash and does not contain `Clave1!`

#### Scenario: Verification against the hash
- **WHEN** `USER DELETE` or `USER UPDATE` is processed with the correct password and with an incorrect one
- **THEN** the correct password is accepted and the incorrect one is rejected without changes

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds and the application restarts
- **THEN** a new hash is stored, `Nueva2#` authenticates, and `Clave1!` no longer does

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected before any hash is produced


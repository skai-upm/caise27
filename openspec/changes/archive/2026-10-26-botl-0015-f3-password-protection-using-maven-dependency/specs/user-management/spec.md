# Spec Delta

## ADDED Requirements

### Requirement: Password protection
Passwords SHALL be stored only as BCrypt hashes. A new password (on creation or on a PASSWORD update) SHALL first satisfy the password validation rules and then be hashed before being stored. Operations requiring authentication (`USER DELETE`, `USER UPDATE`) SHALL verify the supplied password against the stored hash. A plaintext password found when loading stored users SHALL be replaced by its hash.

#### Scenario: Stored as hash
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the users file contains a BCrypt hash instead of `Clave1!` in Ana's password field

#### Scenario: Authentication against the hash
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the password is accepted, and `USER DELETE;ana@correo.com;Clave2!` would be rejected

#### Scenario: Password change persists a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored value is a new hash that verifies `Nueva2#` and no longer verifies `Clave1!`

#### Scenario: Format rules still apply
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva2#` is processed
- **THEN** the change is rejected and the stored hash is unchanged

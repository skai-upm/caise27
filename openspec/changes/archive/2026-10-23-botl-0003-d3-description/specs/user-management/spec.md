# Spec Delta

## ADDED Requirements

### Requirement: Password protection
Passwords SHALL be validated with the password rules and then stored only as a BCrypt hash, never in plain text. Operations that authenticate a user (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt. Changing the password SHALL store a newly generated hash. Stored values that are not valid BCrypt hashes SHALL fail authentication.

#### Scenario: Hash stored
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the stored password value is a BCrypt hash, not `Clave1!`, and `USER DELETE;ana@correo.com;Clave1!` would authenticate

#### Scenario: Password change
- **WHEN** Ana changes PASSWORD to `Nueva2#`
- **THEN** a new hash is stored, `Nueva2#` authenticates and `Clave1!` no longer does

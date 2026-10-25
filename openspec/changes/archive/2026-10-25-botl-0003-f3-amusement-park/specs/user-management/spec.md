# Spec Delta

## ADDED Requirements

### Requirement: Password protection
Passwords SHALL be stored only as BCrypt hashes. The password format rules SHALL be checked on the plaintext before hashing. `USER DELETE` and `USER UPDATE` SHALL authenticate by verifying the supplied password against the stored hash, and a password change SHALL store a new hash.

#### Scenario: Authentication with hash
- **WHEN** Ana was created with password `Clave1!` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the password is verified against the stored hash and the user is deleted

#### Scenario: Password change stores a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored value is a new BCrypt hash that verifies `Nueva2#` and not `Clave1!`

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** an `ERROR` result is printed and nothing is stored

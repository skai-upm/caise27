# Spec Delta

## Purpose

Protects user passwords by storing only BCrypt hashes and verifying supplied passwords against them.

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
After passing the E1 password format rules, every new or changed password SHALL be stored as a BCrypt hash; the plaintext password SHALL NOT be stored. A password failing the E1 rules SHALL be rejected before hashing.

#### Scenario: No plaintext stored
- **WHEN** a user is created with password `Clave1!`
- **THEN** the stored password value is a BCrypt hash and the user file does not contain `Clave1!`

#### Scenario: Format rules still apply
- **WHEN** a user is created with password `clave1!`
- **THEN** creation is rejected

### Requirement: Authentication against stored hash
`USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored hash, permitting the operation only when it matches.

#### Scenario: Correct and incorrect password
- **GIVEN** user `ana@correo.com` created with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Wrong1!;NAME;X` and then `USER UPDATE;ana@correo.com;Clave1!;NAME;X` are executed
- **THEN** the first is rejected and the second succeeds

### Requirement: Password change stores new hash
Changing the password with `USER UPDATE` SHALL persist a new BCrypt hash that authenticates the new password after a restart and no longer accepts the old one.

#### Scenario: Changed password after restart
- **GIVEN** the password was changed from `Clave1!` to `Nueva2#`
- **WHEN** the application restarts
- **THEN** `Nueva2#` authenticates, `Clave1!` does not, and the file holds a hash

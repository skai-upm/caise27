# Spec Delta

## Purpose

Protects user passwords by storing only BCrypt hashes and verifying supplied passwords against them.

## ADDED Requirements

### Requirement: Hashed passwords
Passwords SHALL be stored as BCrypt hashes produced by a library declared as a Maven dependency; plaintext passwords SHALL NOT be stored. The E1 password format rules SHALL be checked on the plaintext before hashing.

#### Scenario: Created user stores a hash
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file does not contain `Clave1!` and contains a BCrypt hash in the password field

#### Scenario: Format still enforced
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the system reports an error

### Requirement: Password verification
`USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored hash and reject wrong passwords.

#### Scenario: Correct and wrong password
- **WHEN** Ana's password is `Clave1!` and `USER UPDATE;ana@correo.com;Mala1!;NAME;X` then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the update is rejected and the deletion succeeds

### Requirement: Password change
Changing a password with `USER UPDATE` SHALL persist a new hash, after which only the new password is accepted.

#### Scenario: New hash persisted
- **WHEN** Ana changes her password to `Nueva2#` and the application restarts
- **THEN** `Nueva2#` is accepted and `Clave1!` is rejected

### Requirement: Legacy plaintext migration
Users whose stored password is not a BCrypt hash SHALL have it hashed when loaded, and the file SHALL be rewritten without plaintext.

#### Scenario: Old file migrated
- **WHEN** the users file contains a line with plaintext password `Clave1!` and the application starts
- **THEN** that user can authenticate with `Clave1!` and the file no longer contains `Clave1!`

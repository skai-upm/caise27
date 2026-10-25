# Spec Delta

## Purpose

Prevents plaintext password storage by hashing passwords with BCrypt and verifying credentials against the stored hash.

## ADDED Requirements

### Requirement: Hashed passwords
Passwords SHALL be stored only as BCrypt hashes produced by a BCrypt library declared as a Maven dependency. The E1 password format rules SHALL be validated on the plaintext before hashing.

#### Scenario: No plaintext stored
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file does not contain `Clave1!` and contains a BCrypt hash in the password field

#### Scenario: Format still enforced
- **WHEN** a user is created with password `clave1!`
- **THEN** creation is rejected

### Requirement: Verification against the hash
USER DELETE and USER UPDATE SHALL verify the supplied password against the stored hash; a password change SHALL persist a new hash.

#### Scenario: Correct and wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` and then `USER DELETE;ana@correo.com;Clave1!` are executed
- **THEN** the first is rejected and the second succeeds

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is executed and the application restarts
- **THEN** the stored hash changed, `Nueva2#` authenticates and `Clave1!` does not

### Requirement: Legacy plaintext migration
Stored passwords that are not BCrypt hashes (from earlier deliveries) SHALL be hashed when loaded and the file rewritten, so that existing users can still authenticate.

#### Scenario: Old data file
- **WHEN** the users file contains a plaintext password `Clave1!` and the application starts
- **THEN** the file is rewritten with a hash and `Clave1!` still authenticates

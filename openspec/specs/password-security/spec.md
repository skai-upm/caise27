# password-security Specification

## Purpose

Protects stored user passwords by hashing them with BCrypt and verifying credentials against the hash.

## Requirements

### Requirement: BCrypt dependency
The `pom.xml` SHALL declare a BCrypt library dependency used to hash and verify passwords.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the BCrypt dependency

### Requirement: Passwords stored as hashes
New and changed passwords SHALL be stored only as BCrypt hashes, never in plaintext. Passwords still stored in plaintext by earlier versions SHALL be replaced by their hash when loaded.

#### Scenario: Create user
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file contains a BCrypt hash instead of `Clave1!`

#### Scenario: Legacy plaintext
- **WHEN** the users file contains a plaintext password from an earlier version and the application starts
- **THEN** the password is stored as a hash and the user can still authenticate with the original password

### Requirement: Authentication against the hash
`USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored hash.

#### Scenario: Correct and wrong password
- **WHEN** `USER DELETE` is processed with the right password and with a wrong one
- **THEN** the right password is accepted and the wrong one is rejected with an `ERROR`

### Requirement: Password change
Changing the password SHALL validate the E1 format rules on the plaintext value first and SHALL persist a new hash.

#### Scenario: Valid change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes and `Nueva2#` authenticates Ana while `Clave1!` no longer does

#### Scenario: Weak new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave` is processed
- **THEN** an `ERROR` is reported and the stored hash is unchanged

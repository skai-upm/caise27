# Spec Delta

## Purpose

Defines how user passwords are protected with BCrypt so that the original password is never stored, and how authenticated operations verify them.

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
After the Delivery 1 password rules are checked, only a BCrypt hash of the password SHALL be stored; the users file SHALL NOT contain the original password. A password change SHALL store a newly generated hash.

#### Scenario: File holds a hash
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file does not contain `Clave1!` and its password field is a BCrypt hash that verifies `Clave1!`

#### Scenario: Password change re-hashed
- **WHEN** USER UPDATE changes the password to `Nueva2#`
- **THEN** a new hash is stored and only `Nueva2#` authenticates afterwards

### Requirement: Password verification with BCrypt
USER DELETE and USER UPDATE SHALL verify the supplied password against the stored hash using the BCrypt library and reject a mismatch.

#### Scenario: Wrong password rejected
- **WHEN** USER DELETE is processed with an incorrect password
- **THEN** an `ERROR:` message is printed and the user is kept

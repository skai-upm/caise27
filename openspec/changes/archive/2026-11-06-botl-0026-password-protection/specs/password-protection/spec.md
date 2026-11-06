# Spec Delta

## Purpose

Protects Amusement Park user passwords with BCrypt so that only protected values are stored, and verifies passwords of authenticated operations against those values.

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
A new or changed password SHALL first satisfy the password validation rules and then be stored only as a BCrypt hash produced by the BCrypt library added through Maven; the persistence file MUST NOT contain the original password.

#### Scenario: Created user file holds a hash
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the user's line in the user file has a BCrypt hash (starting with `$2a$`) in the password field and does not contain `Clave1!`

#### Scenario: Invalid password rejected before hashing
- **WHEN** a user is created with password `clave1!`
- **THEN** it is rejected as an invalid password and nothing is stored

### Requirement: Password verification with BCrypt
Operations that require the user's password, such as `USER DELETE` and `USER UPDATE`, SHALL verify the entered password against the stored hash using the BCrypt library; a wrong password MUST be rejected and change nothing.

#### Scenario: Correct password after restart
- **GIVEN** Ana was created with password `Clave1!` in a previous run
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the update succeeds

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Clave2!` is processed for Ana
- **THEN** an error reporting an incorrect password is printed and Ana still exists

### Requirement: New hash on password change
Changing the password with `USER UPDATE` SHALL validate the new password, generate a new BCrypt hash and persist it; afterwards only the new password verifies.

#### Scenario: Password changed
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes, `Nueva2#` verifies and `Clave1!` no longer does

### Requirement: No legacy migration
Automatic migration of user files written by earlier versions is NOT required; a stored value that is not a BCrypt hash MUST simply fail verification without stopping the application.

#### Scenario: Legacy plain-text password
- **GIVEN** a user file line written by an earlier version with plain-text password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error reporting an incorrect password is printed and processing continues

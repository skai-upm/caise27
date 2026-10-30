# Spec Delta

## Purpose

Protects stored user passwords with BCrypt and verifies authenticated operations against the protected value.

## ADDED Requirements

### Requirement: Store only BCrypt hashes
A new or changed password SHALL first pass the Delivery 1 password rules and then be stored only as a BCrypt hash; the original password SHALL never be written to the persistence file.

#### Scenario: Created user's file line
- **WHEN** `USER CREATE` succeeds with password `Clave1!`
- **THEN** the users file does not contain `Clave1!` and its password field is a BCrypt hash matching `Clave1!`

#### Scenario: Password change re-hashed
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored hash matches `Nueva2#` and no longer matches `Clave1!`

### Requirement: Verify passwords with BCrypt
Operations requiring a password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt, including after restart.

#### Scenario: Correct password after restart
- **WHEN** the application restarts and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** `USER UPDATED` is printed

#### Scenario: Wrong password
- **WHEN** a wrong password is given
- **THEN** an error is printed and nothing changes

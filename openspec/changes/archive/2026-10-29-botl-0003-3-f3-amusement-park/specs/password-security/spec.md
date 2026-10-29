# Spec Delta

## Purpose

Defines how user passwords are protected with BCrypt after validation and verified for authenticated operations.

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
After the Delivery 1 password rules pass, the password SHALL be stored only as its BCrypt hash, generated with a BCrypt library added through Maven; the original password MUST NOT appear in the persistence file. A changed password SHALL get a new hash.

#### Scenario: Hash persisted
- **WHEN** Ana is created with password `Clave1!`
- **THEN** the users file contains a BCrypt hash for Ana and not the text `Clave1!`

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** a new hash is stored and only `Nueva2#` authenticates afterwards

### Requirement: Password verification
Operations requiring a password (USER DELETE, USER UPDATE) SHALL verify the entered password against the stored hash with the BCrypt library.

#### Scenario: Wrong password
- **WHEN** USER DELETE is processed with a wrong password
- **THEN** the user is not deleted and an error is shown

# password-security Specification

## Purpose

Protects user passwords at rest with BCrypt while keeping password-based authentication of user operations.

## Requirements

### Requirement: Hashed password storage
A password SHALL be validated with the Delivery 1 rules and then stored only as a BCrypt hash; the original password SHALL never be written to the data files. Changing the password SHALL store a newly generated hash.

#### Scenario: No plain text on disk
- **WHEN** a user is created with password `Clave1!`
- **THEN** `users.txt` does not contain `Clave1!` and contains a BCrypt hash in the password position

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** a new hash is stored and only `Nueva2#` authenticates Ana afterwards

### Requirement: Password verification
Operations that require a password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt, also after a restart. A stored value that is not a valid BCrypt hash SHALL never authenticate.

#### Scenario: Verification after restart
- **WHEN** Ana is created, the application is restarted, and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the update succeeds

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** an error is printed and Ana is not deleted

# Spec Delta

## MODIFIED Requirements

### Requirement: User data
Each user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created. All fields SHALL be non-empty. The password SHALL be kept only as a BCrypt hash, never in plain text.

#### Scenario: All fields stored
- **WHEN** a user is created with valid data
- **THEN** first name, surname(s), DNI, email, telephone and card number are kept exactly as supplied, and the password is kept as a BCrypt hash that verifies against the supplied password

## ADDED Requirements

### Requirement: Password hashing and verification
After passing the password format validation, a new password (on `USER CREATE` or `USER UPDATE` of the `PASSWORD` field) SHALL be stored as a new BCrypt hash. Operations requiring authentication (`USER DELETE`, `USER UPDATE`) SHALL verify the supplied password against the stored hash.

#### Scenario: Correct password after hashing
- **WHEN** Ana was created with `Clave1!` and `USER DELETE;ana@correo.com;Clave1!` is received (no current tickets)
- **THEN** the password is accepted and Ana is deleted

#### Scenario: Wrong password
- **WHEN** `USER UPDATE;ana@correo.com;Otra1!;NAME;Ana Maria` is received
- **THEN** an authentication error is printed and nothing changes

#### Scenario: Password change persists a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is received
- **THEN** the stored hash changes, `Nueva2#` is accepted afterwards and `Clave1!` is rejected, also after a restart

#### Scenario: Format rules still apply
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave12` is received
- **THEN** a password validation error is printed and the stored hash is unchanged

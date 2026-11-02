# Spec Delta

## Purpose

Protects stored user passwords with BCrypt and verifies authenticated operations against the protected value.

## ADDED Requirements

### Requirement: BCrypt-protected storage
A new password SHALL first satisfy the Delivery 1 password rules and SHALL then be stored only as a BCrypt hash produced by a BCrypt library added as a Maven dependency; the persistence file SHALL NOT contain the original password.

#### Scenario: Registered user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the users file does not contain `Clave1!` and BCrypt verification of `Clave1!` against the stored value succeeds

#### Scenario: Invalid password still rejected
- **WHEN** a user is created with password `clave1!`
- **THEN** creation is rejected before anything is hashed or stored

### Requirement: Verification against the stored hash
`USER DELETE` and `USER UPDATE` SHALL verify the entered password against the stored BCrypt value. Changing the password SHALL generate and persist a new protected value. Automatic migration of plaintext user files from earlier versions is not required.

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** later operations authenticate with `Nueva2#` and reject `Clave1!`

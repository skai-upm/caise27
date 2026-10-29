# Spec Delta

## Purpose

Protects user passwords by storing only BCrypt hashes and verifying entered passwords against them in every authenticated operation.

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
A password SHALL be validated with the Delivery 1 rules and then stored only as a BCrypt hash produced with the `org.mindrot:jbcrypt:0.4` library; the original password SHALL NOT appear in memory-held user data or persistence files.

#### Scenario: Hash stored
- **WHEN** user Ana is created with password `Clave1!`
- **THEN** `users.txt` contains a BCrypt hash (starting with `$2a$`) in the password field and does not contain `Clave1!`

#### Scenario: Invalid password rejected before hashing
- **WHEN** a user is created with password `clave1!`
- **THEN** an error is reported and no user is stored

### Requirement: Verification against stored hash
Operations that require a password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash using the BCrypt library.

#### Scenario: Correct and incorrect passwords after restart
- **WHEN** Ana was created with `Clave1!` and the application is restarted
- **THEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` succeeds and `USER DELETE;ana@correo.com;Mala1!` is rejected

### Requirement: Password change stores a new hash
Changing the password with `USER UPDATE … ;PASSWORD;<new>` SHALL validate the new password, store a new hash and make only the new password valid.

#### Scenario: Password change
- **WHEN** Ana changes her password from `Clave1!` to `Nueva2#`
- **THEN** the stored hash changes, `Nueva2#` is accepted and `Clave1!` is rejected

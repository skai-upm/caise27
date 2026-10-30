# Spec Delta

## Purpose

Protects user passwords by storing BCrypt hashes instead of plaintext and verifying supplied passwords against them.

## ADDED Requirements

### Requirement: BCrypt password storage
The project SHALL declare a BCrypt library in `pom.xml`. After the E1 password format validation, new and changed passwords SHALL be stored only as BCrypt hashes; a password change SHALL store a new hash.

#### Scenario: No plaintext stored
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is executed
- **THEN** `users.txt` does not contain `Clave1!` and stores a BCrypt hash in the password field

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected before hashing

#### Scenario: Password change persists a new hash
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is executed and the application restarts
- **THEN** the stored hash differs from the previous one, `Nueva2#` authenticates and `Clave1!` does not

### Requirement: Password verification
Operations that require authentication SHALL verify the supplied password against the stored hash. Stored values that are not BCrypt hashes (plaintext entries written by earlier deliveries) SHALL be compared literally and SHALL NOT be migrated automatically.

#### Scenario: Correct and incorrect password
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` and then `USER DELETE;ana@correo.com;Clave1!` are executed
- **THEN** the first is rejected and the second deletes Ana

#### Scenario: Legacy plaintext entry
- **GIVEN** a `users.txt` line written by Delivery 2 with plaintext password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is executed
- **THEN** the update succeeds

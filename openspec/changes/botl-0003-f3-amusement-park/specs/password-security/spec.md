# Spec Delta

## Purpose

Prevents plaintext storage of user passwords by protecting them with BCrypt and verifying supplied passwords against the stored value.

## ADDED Requirements

### Requirement: BCrypt dependency
The project SHALL declare a Maven dependency providing BCrypt hashing and SHALL bundle it so the executable JAR runs without extra classpath configuration.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** a BCrypt library dependency is declared and `java -jar target/parque.jar` runs

### Requirement: Passwords stored as hashes
When a user is created or a password is changed, the password SHALL first pass the E1 format validation and SHALL then be stored and persisted only as a BCrypt hash, never in plaintext. A changed password SHALL produce a new hash.

#### Scenario: Creation stores a hash
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the users file contains a BCrypt hash in the password field and does not contain `Clave1!`

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the request is rejected and no hash is stored

#### Scenario: Password change persists a new hash
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed and the application restarts
- **THEN** the stored value is a new hash, `Nueva2#` authenticates Ana, and `Clave1!` no longer does

### Requirement: Password verification
`USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored protected value. A stored value that is not a BCrypt hash (written by an earlier delivery) SHALL be verified by exact comparison and SHALL NOT be migrated silently.

#### Scenario: Correct and incorrect password
- **GIVEN** Ana stored with a hash of `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the first is rejected and the second deletes Ana

#### Scenario: Legacy plaintext user
- **GIVEN** a users file written by E1 with plaintext password `Clave1!` for Ana
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the update succeeds and the stored password value is left unchanged

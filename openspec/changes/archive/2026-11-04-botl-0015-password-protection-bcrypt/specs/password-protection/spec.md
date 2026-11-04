# Spec Delta

## Purpose

Protects user passwords by storing only BCrypt hashes, produced with an external library declared through Maven, and verifying supplied passwords against those hashes.

## ADDED Requirements

### Requirement: BCrypt dependency
The Maven build SHALL declare a BCrypt library dependency in `pom.xml`, and password hashing SHALL use that library's salted hash and verify functions.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the BCrypt library dependency and `mvn package` succeeds

### Requirement: Passwords stored as hashes
Passwords of created users and changed passwords SHALL be stored as BCrypt hashes and never in plaintext. Each hashing SHALL use a new random salt.

#### Scenario: New user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the stored password is a BCrypt hash that verifies `Clave1!` and differs from `Clave1!`

#### Scenario: Password change persists a new hash
- **GIVEN** Ana's stored hash H1
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored value is a different hash that verifies `Nueva2#` and no longer verifies `Clave1!`

### Requirement: Format validation before hashing
The E1 password rules (at least one uppercase letter, lowercase letter, digit and symbol) SHALL be checked on the plaintext before hashing, on creation and on password change.

#### Scenario: Weak password rejected
- **WHEN** a user is created with `Clave12`, or a password update uses `clave1!`
- **THEN** the command is rejected and nothing is hashed or stored

### Requirement: Authentication against hashes
`USER DELETE` and `USER UPDATE` SHALL authenticate by verifying the supplied password against the stored hash; a wrong password SHALL be rejected without changes.

#### Scenario: Correct and wrong passwords
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;NAME;X` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the update is rejected and the deletion succeeds

### Requirement: Legacy plaintext records
Stored password values that are not BCrypt hashes, such as plaintext records written by earlier versions, SHALL NOT be migrated automatically and SHALL never authenticate; the application SHALL treat them as a non-matching password without failing.

#### Scenario: Plaintext record from an older file
- **GIVEN** a users file line written by an earlier version with plaintext password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR` message about invalid credentials is produced and the application keeps running

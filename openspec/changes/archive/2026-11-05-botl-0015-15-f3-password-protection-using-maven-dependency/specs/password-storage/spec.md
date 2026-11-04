# Spec Delta

## Purpose

Ensures user passwords are never stored in plaintext: they are kept as BCrypt hashes produced by an external Maven library and supplied passwords are verified against those hashes.

## ADDED Requirements

### Requirement: BCrypt dependency
The Maven build SHALL declare a BCrypt library dependency, and password hashing and verification SHALL use it.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the dependency `org.mindrot:jbcrypt`

### Requirement: Passwords stored as hashes
When a user is created or changes its password, the system SHALL first validate the plaintext password with the existing password rules and then store only its BCrypt hash, generated with a new random salt; the plaintext password SHALL NOT be stored.

#### Scenario: Created user's password not in plaintext
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the users file does not contain `Clave1!` and Ana's password field is a BCrypt hash (starting with `$2a$`) that verifies `Clave1!`

#### Scenario: Invalid password not hashed
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the command is rejected by the password rules and nothing is stored

#### Scenario: Password change stores a new hash
- **GIVEN** Ana's stored hash `H1`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds and the application restarts
- **THEN** the stored hash differs from `H1`, `Nueva2#` authenticates and `Clave1!` no longer does

### Requirement: Verification against stored hashes
`USER DELETE` and `USER UPDATE` SHALL authenticate by verifying the supplied current password against the stored hash; a wrong password, or a stored value that is not a valid BCrypt hash, SHALL be treated as an authentication failure without changing data.

#### Scenario: Delete with correct or wrong password
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the first is rejected and the second deletes Ana

#### Scenario: Update with wrong current password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999` is processed
- **THEN** it is rejected and the stored data is unchanged

#### Scenario: Legacy plaintext record
- **GIVEN** a users file line written before this change with plaintext password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is rejected as an authentication failure and the application does not terminate

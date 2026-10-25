# Spec Delta

## Purpose

Defines how user passwords are protected: stored only as BCrypt hashes produced by an external Maven library, verified on authentication, and migrated from plaintext.

## ADDED Requirements

### Requirement: BCrypt dependency
The project SHALL declare a BCrypt library as a dependency in `pom.xml` and use it to hash and verify passwords.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the `org.mindrot:jbcrypt` dependency

### Requirement: Passwords stored as hashes
New and changed passwords SHALL be stored only as BCrypt hashes, never in plaintext. The password format rules (uppercase, lowercase, digit, symbol) SHALL be checked on the plaintext before hashing.

#### Scenario: Created user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the stored password is a BCrypt hash that verifies `Clave1!` and the text `Clave1!` does not appear in the users file

#### Scenario: Format still enforced
- **WHEN** `USER CREATE` is processed with password `clave1!`
- **THEN** the response is `ERROR INVALID PASSWORD`

### Requirement: Password verification
`USER DELETE` and `USER UPDATE` SHALL authenticate by verifying the supplied password against the stored hash; a wrong password gives `ERROR INVALID CREDENTIALS`.

#### Scenario: Delete with correct and wrong password
- **WHEN** Ana exists with password `Clave1!` and `USER DELETE;ana@correo.com;Otra1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the responses are `ERROR INVALID CREDENTIALS` and `OK USER DELETED`

### Requirement: Password change stores a new hash
`USER UPDATE;…;PASSWORD;<new>` SHALL validate the new password format and persist a new hash, after which only the new password authenticates.

#### Scenario: Change password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored hash changes, `Nueva2#` authenticates and `Clave1!` no longer does, also after restarting

### Requirement: Plaintext migration
When users are loaded and a stored password is not a BCrypt hash, it SHALL be replaced by its hash and the users file rewritten.

#### Scenario: Legacy file
- **WHEN** `data/users.txt` contains Ana with plaintext password `Clave1!` and the application starts
- **THEN** the file now holds a hash for Ana and `USER DELETE;ana@correo.com;Clave1!` still succeeds

# password-protection Specification

## Purpose

Protects user passwords by storing only BCrypt hashes, produced with an external library declared in the Maven build, and by verifying supplied passwords against those hashes.

## Requirements

### Requirement: BCrypt dependency
The Maven build SHALL declare a BCrypt library as a compile dependency, and password hashing and verification SHALL use that library's salted hash and check functions.

#### Scenario: Dependency declared
- **WHEN** `pom.xml` is inspected
- **THEN** it declares the BCrypt library dependency and `mvn package` builds with it

### Requirement: Passwords stored as hashes
When a user is created, the system SHALL validate the plaintext password with the existing password rules and then store only its BCrypt hash. Two users with the same password SHALL get different hashes (random salt).

#### Scenario: Persisted secret
- **GIVEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeded
- **WHEN** the users file is inspected
- **THEN** it does not contain `Clave1!` and Ana's password field is a BCrypt hash

#### Scenario: Validation before hashing
- **WHEN** `USER CREATE` is processed with the password `clave1!` (no uppercase letter)
- **THEN** the creation is rejected with `ERROR` exactly as before and nothing is stored

### Requirement: Authentication against the stored hash
`USER DELETE`, `USER UPDATE` and any other operation that authenticates a user SHALL accept the request only if the supplied password verifies against the stored hash. A wrong password SHALL be rejected without changing any data.

#### Scenario: Authenticated delete
- **GIVEN** Ana exists with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the first is rejected with `ERROR` and Ana remains, and the second deletes her

#### Scenario: Authenticated update
- **WHEN** `USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999` is processed
- **THEN** it is rejected with `ERROR` and the stored user is unchanged

### Requirement: Password change stores a new hash
A successful `USER UPDATE ... ;PASSWORD;<nueva>` SHALL validate the new password with the existing rules and persist a new BCrypt hash of it. After that, only the new password SHALL authenticate, also after a restart.

#### Scenario: Password rotation across restart
- **GIVEN** Ana changed her password from `Clave1!` to `Nueva2#`
- **WHEN** the application restarts
- **THEN** `USER UPDATE;ana@correo.com;Nueva2#;PHONE;699999999` succeeds, a request with `Clave1!` is rejected, and the stored hash differs from the previous one

#### Scenario: Invalid new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva` is processed
- **THEN** it is rejected with `ERROR` and the stored hash is unchanged

### Requirement: Stored values that are not hashes
A stored password value that is not a valid BCrypt hash (for example plaintext written by an earlier version) SHALL NOT authenticate any request and SHALL NOT cause an unhandled error. No automatic migration is performed.

#### Scenario: Legacy plaintext entry
- **GIVEN** the users file contains a user line whose password field is the plaintext `Clave1!`
- **WHEN** `USER DELETE;<that email>;Clave1!` is processed
- **THEN** the result starts with `ERROR` and the user remains stored

# Spec Delta

## Purpose

Protects user passwords by storing only BCrypt hashes, produced by an external library declared in Maven, and verifying every authentication against the stored hash.

## ADDED Requirements

### Requirement: BCrypt dependency in Maven
The Maven build SHALL declare a BCrypt library dependency in `pom.xml`, and the packaged executable JAR SHALL include it so `java -jar` runs without extra classpath settings.

#### Scenario: Declared and packaged
- **WHEN** `pom.xml` is inspected and `mvn package` is run
- **THEN** a BCrypt dependency is declared and `java -jar` on the produced JAR can create and authenticate users

### Requirement: Passwords stored as hashes
New and changed passwords SHALL be stored only as BCrypt hashes, never as plaintext.

#### Scenario: Persisted secret
- **GIVEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeded
- **WHEN** the user file is inspected
- **THEN** it does not contain `Clave1!`, and Ana's password field is a BCrypt hash (starting with `$2`) that verifies `Clave1!`

#### Scenario: Different hashes for equal passwords
- **WHEN** two users are created with the same password
- **THEN** their stored hashes differ (salted)

### Requirement: Authentication against the stored hash
Operations that authenticate with email and password (`USER DELETE`, `USER UPDATE`) SHALL succeed only when the supplied password matches the stored hash; a stored value that is not a valid hash SHALL never match.

#### Scenario: Authenticated delete
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the first is rejected with `ERROR:` and the second deletes Ana

#### Scenario: Wrong current password on update
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999` is processed
- **THEN** an `ERROR:` response is printed and the stored data is unchanged

#### Scenario: Legacy plaintext record
- **GIVEN** a user file line whose password field is the plaintext `Clave1!`
- **WHEN** that user tries to authenticate with `Clave1!`
- **THEN** the authentication is rejected (no automatic migration)

### Requirement: Password change stores a new hash
A successful `USER UPDATE;…;PASSWORD;<nueva>` SHALL validate the new password with the E1 rules and persist a new hash; afterwards only the new password SHALL authenticate, also after a restart.

#### Scenario: Password rotation survives restart
- **GIVEN** Ana changed her password from `Clave1!` to `Nueva2#`
- **WHEN** the application restarts and `USER UPDATE;ana@correo.com;Nueva2#;PHONE;699999999` is processed
- **THEN** it succeeds, and the same update with `Clave1!` is rejected

#### Scenario: E1 rules still apply
- **WHEN** a user is created with password `clave1!` or a password is changed to `Clave12`
- **THEN** the operation is rejected and nothing is hashed or stored

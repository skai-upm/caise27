# password-protection Specification

## Purpose

Protects user passwords by storing only salted BCrypt hashes and verifying supplied passwords against them.

## Requirements

### Requirement: BCrypt dependency
The Maven build SHALL declare the BCrypt library as a dependency, and the executable JAR produced by `mvn package` SHALL include it so the application runs with `java -jar`.

#### Scenario: Dependency declared and bundled
- **WHEN** `pom.xml` is inspected and the packaged JAR is run
- **THEN** the BCrypt dependency is declared and the application starts and creates users without class-loading errors

### Requirement: Passwords stored as hashes
New passwords (on user creation and on password change) SHALL first satisfy the E1 password rules and SHALL then be stored as a BCrypt hash with a fresh salt; the plaintext password SHALL NOT be stored.

#### Scenario: Created user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the user file does not contain `Clave1!` and its password field is a BCrypt hash

#### Scenario: E1 rules still apply
- **WHEN** a creation or password change uses `Clave12` (no symbol)
- **THEN** it is rejected and nothing is hashed or stored

#### Scenario: Password change stores a new hash
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds and the application restarts
- **THEN** the stored hash differs from the previous one, `Nueva2#` authenticates and `Clave1!` does not

### Requirement: Verification against the stored hash
`USER DELETE` and `USER UPDATE` SHALL authenticate by checking the supplied password against the stored hash; a wrong password SHALL be rejected without changing data. A stored value that is not a BCrypt hash (legacy plaintext record) SHALL never authenticate.

#### Scenario: Delete with correct and incorrect password
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the first is rejected and the second deletes Ana

#### Scenario: Update with incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999` is processed
- **THEN** it is rejected and the stored data is unchanged

#### Scenario: Legacy plaintext record
- **GIVEN** a user file line whose password field is the plaintext `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is rejected as an incorrect password

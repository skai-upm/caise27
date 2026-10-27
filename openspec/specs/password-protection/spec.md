# password-protection Specification

## Purpose

Protects user passwords with BCrypt: only protected values are stored, and authenticated operations verify the entered password against them.

## Requirements

### Requirement: Passwords stored as BCrypt values
New and changed passwords SHALL first satisfy the Delivery 1 password rules and then be stored only as a newly generated BCrypt value; the users file SHALL NOT contain the original password.

#### Scenario: Created user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the stored password is a BCrypt value (starting with `$2a$`) and `users.txt` does not contain `Clave1!`

#### Scenario: Invalid password still rejected
- **WHEN** USER CREATE uses the password `clave1!`
- **THEN** an invalid-password error is reported and nothing is stored

#### Scenario: Password change generates a new value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** a new BCrypt value is stored and persisted, `Nueva2#` authenticates and `Clave1!` no longer does

### Requirement: Verification with BCrypt
Operations that require the password (USER DELETE, USER UPDATE) SHALL verify the entered password against the stored BCrypt value using the BCrypt library.

#### Scenario: Correct password
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed for Ana
- **THEN** the password is accepted

#### Scenario: Wrong password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999` is processed
- **THEN** an incorrect-password error is reported

#### Scenario: Verification after restart
- **WHEN** Ana is created, the application restarts and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the response is `USER UPDATED`

### Requirement: Library added through Maven
The BCrypt library SHALL be declared as a Maven dependency and the executable JAR SHALL run with `java -jar target/nombre-aplicacion.jar` without additional classpath configuration.

#### Scenario: Self-contained JAR
- **WHEN** `mvn package` is run and the JAR creates a user
- **THEN** the user is created without class-loading errors

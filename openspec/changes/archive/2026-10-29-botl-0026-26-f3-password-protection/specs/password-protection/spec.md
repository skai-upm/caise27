# Spec Delta

## Purpose

Defines how user passwords are protected: validated with the password rules, stored only as BCrypt hashes, and verified against those hashes in operations that require the user's password.

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
When a user is created or changes its password, the password SHALL first be validated with the password rules and then only its BCrypt hash SHALL be stored in memory and in the users file; the original password SHALL never be written to the file.

#### Scenario: Users file after creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** Ana's line in `users.txt` has a BCrypt value (starting with `$2a$`) in the password column and does not contain `Clave1!`

#### Scenario: Invalid password not stored
- **WHEN** `USER CREATE` is processed with password `clave1!`
- **THEN** an `ERROR:` line about the password is printed and nothing is stored

### Requirement: Verification against the stored hash
`USER DELETE` and `USER UPDATE` SHALL verify the entered password against the stored BCrypt hash using the library; a wrong password SHALL be rejected with an `ERROR:` message.

#### Scenario: Correct and wrong password
- **WHEN** Ana was created with `Clave1!` and `USER UPDATE;ana@correo.com;Mala1!;PHONE;6` and then `USER UPDATE;ana@correo.com;Clave1!;PHONE;6` are processed
- **THEN** the first is rejected as an incorrect password and the second prints `USER UPDATED`

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** a new BCrypt hash is stored and persisted, `Nueva2#` is accepted afterwards and `Clave1!` is rejected, also after restarting the application

### Requirement: Library in the executable JAR
The BCrypt library SHALL be declared as a Maven dependency and included in the executable JAR produced by `mvn package`, so the application runs with `java -jar` alone.

#### Scenario: Standalone JAR
- **WHEN** `java -jar target/amusement-park.jar` runs a file creating and deleting a user
- **THEN** both commands succeed without any additional classpath

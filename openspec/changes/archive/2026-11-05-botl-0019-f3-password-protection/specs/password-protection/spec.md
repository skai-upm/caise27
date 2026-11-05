# Spec Delta

## Purpose

Defines how user passwords are protected with BCrypt before being stored and how operations that require authentication verify an entered password against the stored protected value.

## ADDED Requirements

### Requirement: Passwords stored only as BCrypt values
Before a user password is stored, it SHALL first pass the Delivery 1 password validation rules, and then be converted into a BCrypt-protected value. Only that value SHALL be kept in memory for persistence and written to persistence files; the original password SHALL never be stored. Each protection SHALL use a new random salt.

#### Scenario: Protected value instead of plain text
- **GIVEN** a valid new password `Clave1!`
- **WHEN** it is protected for storage
- **THEN** the stored value is a BCrypt hash (starting with `$2a$`) that is different from and does not contain `Clave1!`

#### Scenario: Same password, different stored values
- **WHEN** the same password is protected twice
- **THEN** the two protected values differ, and both verify against the password

### Requirement: Authenticated operations verify against the stored value
Operations that require password verification, such as `USER DELETE` and `USER UPDATE`, SHALL check the entered password against the stored BCrypt value using the BCrypt library, and SHALL be rejected when it does not match.

#### Scenario: Correct password
- **GIVEN** a stored protected value for `Clave1!`
- **WHEN** `Clave1!` is entered for an authenticated operation
- **THEN** the verification succeeds

#### Scenario: Wrong password
- **GIVEN** a stored protected value for `Clave1!`
- **WHEN** `clave1!` is entered
- **THEN** the verification fails and the operation is rejected

#### Scenario: Stored value that is not a BCrypt hash
- **GIVEN** a stored value that is not a valid BCrypt hash (for example a legacy plain-text password)
- **WHEN** any password is verified against it
- **THEN** the verification fails without terminating the application

### Requirement: Password changes regenerate the protected value
When a user's password is changed, a new protected value SHALL be generated from the new password and persisted, replacing the previous one.

#### Scenario: Password updated
- **GIVEN** a stored protected value for `Clave1!`
- **WHEN** the password is changed to `Nueva2#`
- **THEN** the new stored value verifies `Nueva2#` and no longer verifies `Clave1!`

# password-protection Specification

## Purpose

Defines how user passwords are protected: validated with the password rules, stored only as BCrypt-protected values, and verified against the stored value in authenticated operations.

## Requirements

### Requirement: Passwords stored as BCrypt values
When a user is created, the system SHALL apply the password validation rules to the entered password and then store only its BCrypt-protected value; the original password MUST NOT be kept in memory as the user's password nor written to the persistence file.

#### Scenario: Persisted password is protected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the line for Ana in `users.txt` does not contain `Clave1!` and its password field is a BCrypt value (starting with `$2a$`)

#### Scenario: Invalid password rejected before protection
- **WHEN** a `USER CREATE` with password `Clave12` is processed
- **THEN** an error message starting with `ERROR` is printed and no user is created

### Requirement: Password verification against the stored value
Operations that require the user's password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored BCrypt value using the BCrypt library; a non-matching password MUST be rejected with an error message starting with `ERROR`.

#### Scenario: Correct password accepted
- **GIVEN** Ana registered with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** `USER UPDATED` is printed

#### Scenario: Wrong password or stored hash entered as password rejected
- **GIVEN** Ana registered with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` or `USER DELETE;ana@correo.com;<Ana's stored hash>` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana is still registered

#### Scenario: Verification after restart
- **GIVEN** Ana registered with `Clave1!` in a previous execution
- **WHEN** the application restarts and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed

### Requirement: Password change generates a new protected value
`USER UPDATE;<email>;<password>;PASSWORD;<new>` SHALL validate the new password with the password rules and store a new BCrypt value, after which only the new password authenticates.

#### Scenario: Password changed
- **GIVEN** Ana with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** `USER UPDATED` is printed, the stored value changes and is a BCrypt value, `Nueva2#` authenticates and `Clave1!` no longer does

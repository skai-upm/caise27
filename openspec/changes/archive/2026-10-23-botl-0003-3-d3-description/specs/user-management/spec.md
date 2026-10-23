# Spec Delta

## ADDED Requirements

### Requirement: Password protection
Passwords SHALL be checked against the password rules and then stored only as a BCrypt hash. The plain password SHALL never be kept in memory or on disk. Commands that need the password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored hash with BCrypt. Changing the password with `USER UPDATE;...;PASSWORD;<new>` SHALL validate the new password and store a newly generated hash.

#### Scenario: Stored value is a hash
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the stored password value is a BCrypt hash, not `Clave1!`

#### Scenario: Correct password verified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed for that user
- **THEN** the password is accepted and `USER UPDATED` is printed

#### Scenario: Wrong password rejected
- **WHEN** `USER DELETE;ana@correo.com;clave1!` is processed
- **THEN** an error is printed and the user still exists

#### Scenario: Password change produces a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes, `Nueva2#` is accepted afterwards, and `Clave1!` is rejected

#### Scenario: Invalid new password not stored
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva` is processed
- **THEN** an error is printed and `Clave1!` still verifies

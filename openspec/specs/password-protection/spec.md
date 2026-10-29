# password-protection Specification

## Purpose

Defines how user passwords are protected with BCrypt when stored and how entered passwords are verified for operations that require authentication.

## Requirements

### Requirement: Passwords stored only in protected form
When a user's password is set or changed, the system SHALL store only a BCrypt-protected value generated with the BCrypt library, never the original password; each change SHALL generate a new protected value.

#### Scenario: Protected value stored
- **WHEN** the password `Clave1!` is set for `ana@correo.com`
- **THEN** the stored value is a BCrypt hash different from `Clave1!`

#### Scenario: Change generates a new value
- **WHEN** the password of `ana@correo.com` is changed to `Nueva2#`
- **THEN** a new protected value is stored, `Nueva2#` is accepted and `Clave1!` is rejected

### Requirement: Password verification for authenticated operations
Operations that require password verification SHALL check the entered password against the stored protected value using the BCrypt library; a wrong password, or a user without a stored password, SHALL be rejected with an error.

#### Scenario: Correct and wrong password
- **WHEN** `ana@correo.com` has password `Clave1!` and verification is requested with `Clave1!` and with `clave1!`
- **THEN** the first is accepted and the second is rejected

### Requirement: Persistence keeps only the protected value
The users file SHALL contain the BCrypt-protected value and never the original password, and reloading SHALL allow the password to be verified again.

#### Scenario: Restart keeps verification working
- **WHEN** a user with password `Clave1!` is saved, the application restarts and the password is verified
- **THEN** `Clave1!` is accepted and the users file does not contain `Clave1!`

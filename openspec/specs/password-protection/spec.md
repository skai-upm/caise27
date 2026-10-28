# password-protection Specification

## Purpose
Protects user passwords with BCrypt: rules are checked before storing, only protected values are persisted, and password-checked operations verify against them.

## Requirements

### Requirement: Password rules before storing
Before a new password is protected and stored, the system SHALL require it to contain at least one uppercase letter, one lowercase letter, one digit and one symbol; otherwise the operation SHALL be rejected and nothing stored.

#### Scenario: Valid password accepted
- **WHEN** the password `Clave1!` is set for a user
- **THEN** it is accepted and stored in protected form

#### Scenario: Weak password rejected
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is set for a user
- **THEN** the operation is rejected with an error and the user's stored password is unchanged

### Requirement: Only BCrypt values are stored
The system SHALL keep and persist only the BCrypt-protected value of a password, never the original text. Changing a password SHALL generate and persist a new protected value.

#### Scenario: Persisted user file contains no plain password
- **WHEN** a user with password `Clave1!` is saved
- **THEN** the user file contains a BCrypt value for that user and does not contain `Clave1!`

#### Scenario: Password change produces a new protected value
- **WHEN** a user's password is changed to `Nueva2#`
- **THEN** the stored protected value differs from the previous one and verifies `Nueva2#` but not the old password

### Requirement: Password verification with BCrypt
Operations requiring password verification SHALL check the entered password against the stored protected value using the BCrypt library; a wrong password, an unknown user or a user without a stored password SHALL fail authentication.

#### Scenario: Correct and wrong passwords
- **WHEN** user `ana@correo.com` has password `Clave1!` and authentication is attempted with `Clave1!` and with `Clave1?`
- **THEN** the first succeeds and the second is rejected with an authentication error

#### Scenario: Verification after restart
- **WHEN** the application restarts and authentication of `ana@correo.com` with `Clave1!` is attempted
- **THEN** it succeeds against the persisted BCrypt value

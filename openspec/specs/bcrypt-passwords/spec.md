# bcrypt-passwords Specification

## Purpose

Protects stored user passwords with BCrypt while preserving the Delivery-1 password rules and authenticated operations.

## Requirements

### Requirement: BCrypt-protected storage
After passing the Delivery-1 validation rules, a password SHALL be stored only as a BCrypt hash produced by a BCrypt library added as a Maven dependency; the persistence file SHALL NOT contain the original password.

#### Scenario: No plaintext in storage
- **WHEN** a user is created with password `Clave1!`
- **THEN** the user file does not contain `Clave1!` and contains a BCrypt hash that verifies `Clave1!`

#### Scenario: Invalid password still rejected
- **WHEN** a user is created with password `clave1!`
- **THEN** creation is rejected before any hash is stored

### Requirement: Password verification against hashes
Operations requiring a password, such as `USER DELETE` and `USER UPDATE`, SHALL verify the supplied password against the stored hash; changing the password SHALL generate and persist a new hash.

#### Scenario: Password change
- **WHEN** the password is updated from `Clave1!` to `Nueva2#`
- **THEN** only `Nueva2#` authenticates afterwards, also after a restart

### Requirement: No migration of plaintext files
Automatic migration of user files with plaintext passwords from earlier deliveries SHALL NOT be required; such passwords SHALL simply fail verification.

#### Scenario: Legacy plaintext record
- **WHEN** a stored user line holds a plaintext password and that password is supplied
- **THEN** authentication fails with an error and no crash

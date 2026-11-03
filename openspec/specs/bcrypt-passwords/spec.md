# bcrypt-passwords Specification

## Purpose

Stops storing passwords in plain text: passwords are validated with the Delivery 1 rules and persisted only as BCrypt hashes produced by a Maven dependency (source F3-C05, section 8).

## Requirements

### Requirement: Passwords stored as BCrypt hashes
After passing the Delivery 1 password rules, a new or changed password SHALL be stored only as a BCrypt hash computed with a BCrypt library added as a Maven dependency; the persisted data SHALL NOT contain the original password.

#### Scenario: Plaintext not persisted
- **WHEN** a user is created with password `Clave1!`
- **THEN** the user data file does not contain `Clave1!` and contains a BCrypt hash that verifies `Clave1!`

#### Scenario: Invalid password still rejected
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected by the Delivery 1 rules and nothing is hashed or stored

### Requirement: Password verification against the hash
Operations requiring a password, such as `USER DELETE` and `USER UPDATE`, SHALL verify the supplied password against the stored hash; a password change SHALL generate and persist a new hash.

#### Scenario: Only the new password authenticates after a change
- **WHEN** Ana's PASSWORD is updated from `Clave1!` to `Nueva2#`
- **THEN** a later `USER UPDATE` with `Clave1!` is rejected and one with `Nueva2#` succeeds, also after a restart

### Requirement: No migration of plaintext files
The application SHALL NOT be required to migrate user files written by earlier versions; a stored value that is not a valid BCrypt hash SHALL simply fail verification without terminating the application.

#### Scenario: Legacy plaintext record
- **WHEN** `users.txt` contains a Delivery 2 record with a plaintext password and `USER DELETE` is attempted for it
- **THEN** a controlled error is reported and the application continues

# bcrypt-passwords Specification

## Purpose

Defines protection of stored user passwords with BCrypt provided through a Maven dependency (F3-C05).

## Requirements

### Requirement: Passwords stored as BCrypt hashes
New and changed passwords SHALL first satisfy the Delivery 1 password rules and SHALL then be stored only as BCrypt hashes produced with a BCrypt library added as a Maven dependency. The users file SHALL NOT contain the original password.

#### Scenario: Registration stores a hash
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file does not contain `Clave1!` and the stored value verifies `Clave1!` with BCrypt

#### Scenario: Invalid password still rejected
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected and nothing is stored

### Requirement: Password verification against the hash
Operations that require a password, such as `USER DELETE` and `USER UPDATE`, SHALL verify the supplied password against the stored hash with the BCrypt library. Changing the password SHALL generate and persist a new hash. Migration of earlier plaintext files is not required; a stored value that is not a BCrypt hash SHALL simply fail verification.

#### Scenario: Password change
- **WHEN** Ana changes her password from `Clave1!` to `Nueva2#`
- **THEN** `Nueva2#` authenticates and `Clave1!` no longer does

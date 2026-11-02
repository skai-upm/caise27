# Spec Delta

## Purpose

Protects user passwords by storing only their BCrypt hashes, using a BCrypt library added through Maven, and verifying authenticated operations against those hashes.

## ADDED Requirements

### Requirement: Hashed password storage
A new or changed password SHALL first pass the Delivery 1 password rules and SHALL then be stored and persisted only as a BCrypt hash produced by a BCrypt library declared as a Maven dependency; the persisted files SHALL NOT contain the original password. (F3-C05)

#### Scenario: Registration
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file holds a BCrypt hash for that user, does not contain `Clave1!`, and the hash verifies `Clave1!`

#### Scenario: Invalid password still rejected
- **WHEN** a user is created or updated with `clave1!`
- **THEN** the operation is rejected by the Delivery 1 rules

### Requirement: Hash-based authentication
USER DELETE and USER UPDATE SHALL verify the supplied password against the stored hash; a password change SHALL store a newly generated hash. Migration of earlier plaintext files is not required. (F3-C05)

#### Scenario: Password change
- **WHEN** Ana changes her password from `Clave1!` to `Nueva2#`
- **THEN** only `Nueva2#` authenticates afterwards, including after a restart

# Spec Delta

## ADDED Requirements

### Requirement: Password protection
Passwords SHALL be validated with the E1 password rules and then stored only as BCrypt hashes, both at `USER CREATE` and when `USER UPDATE` changes the `PASSWORD` field (which SHALL store a new hash). The BCrypt library SHALL be declared as a Maven dependency.

#### Scenario: No plaintext stored
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the stored user record contains a BCrypt hash and does not contain `Clave1!`

#### Scenario: Password change stores new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds and the application restarts
- **THEN** the stored hash differs from the previous one and `Nueva2#` authenticates while `Clave1!` does not

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected

### Requirement: Password verification
`USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored BCrypt hash. A stored value that is not a BCrypt hash (written by an earlier delivery) SHALL be compared as legacy plaintext, without automatic migration.

#### Scenario: Correct and incorrect passwords
- **WHEN** `USER DELETE` or `USER UPDATE` is executed with the correct password and then with a wrong one
- **THEN** the correct password is accepted and the wrong one rejected

#### Scenario: Legacy plaintext user
- **WHEN** `users.txt` contains a user written by E1 with plaintext password `Clave1!`
- **THEN** that user can still authenticate with `Clave1!`

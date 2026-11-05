## MODIFIED Requirements

### Requirement: Authentication before modification
Before any modification, the system SHALL locate the user by the given current email and verify the given password against the user's stored BCrypt-protected password with the jBCrypt library. If no user has that email or the password does not match, the command MUST be rejected with an `ERROR` response and no user modified.

#### Scenario: Incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;NAME;Ana Maria` is processed for a user whose password is `Clave1!`
- **THEN** an `ERROR` response is printed and the user's name is unchanged

#### Scenario: Unknown email
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;NAME;X` is processed and no user has that email
- **THEN** an `ERROR` response is printed

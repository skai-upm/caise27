# Spec Delta

## ADDED Requirements

### Requirement: Password verification
Operations that require the user's password (`USER DELETE`, `USER UPDATE`) SHALL verify the entered password against the stored BCrypt hash; password rules SHALL be checked on the plain password before it is hashed; a stored value that is not a valid BCrypt hash SHALL never authenticate.

#### Scenario: Correct password
- **WHEN** Ana was created with `Clave1!` and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** the update succeeds

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Clave1?` is processed
- **THEN** an error is printed and Ana is kept

#### Scenario: Legacy plain-text entry
- **WHEN** the users file contains a Delivery 1 line with a plain-text password
- **THEN** no password authenticates that user

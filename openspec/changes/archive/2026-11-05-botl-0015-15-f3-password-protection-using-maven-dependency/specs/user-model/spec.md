# Spec Delta

## MODIFIED Requirements

### Requirement: User data fields
The system SHALL hold, for every user, a first name, surname(s), DNI, email address, password hash, telephone number and card number, exposed only through the user's own accessors (encapsulated state).

#### Scenario: Created user exposes all fields
- **GIVEN** a valid `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` command
- **WHEN** it is processed
- **THEN** the stored user has first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, a password hash that verifies `Clave1!`, phone `612345678` and card `1234567890123456`

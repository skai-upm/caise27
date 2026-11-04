## MODIFIED Requirements

### Requirement: User data
The system SHALL hold, for each user, a first name, surname(s), DNI, email address, password (kept only as its BCrypt hash), telephone number and card number, accessible only through the user's own operations (encapsulated state).

#### Scenario: All seven fields retained
- **GIVEN** the command `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeded
- **WHEN** the stored user is inspected
- **THEN** it holds first name `Ana`, surname(s) `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, a password hash that verifies `Clave1!` (not the plaintext), phone `612345678` and card `1234567890123456`

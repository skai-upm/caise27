# Spec Delta

## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number, where the password field holds the BCrypt hash of the password. Databases, persistence frameworks and external persistence libraries MUST NOT be used.

#### Scenario: Stored line format
- **WHEN** Ana is created with `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<bcrypt-hash>;612345678;1234567890123456` whose hash verifies `Clave1!`

# Spec Delta

## MODIFIED Requirements

### Requirement: Text-file user storage
The system SHALL store all registered users in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card number. No database or external persistence library SHALL be used.

#### Scenario: File format
- **WHEN** user Ana (`Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`) has been created
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

# Spec Delta

## MODIFIED Requirements

### Requirement: Text-file user storage
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card. Reading and writing SHALL be implemented in a component independent of the user representation and the main application class. No database or external persistence library SHALL be used.

#### Scenario: File format
- **GIVEN** user Ana was created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** the users file is inspected
- **THEN** it contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2` (a BCrypt hash) and ending `;612345678;1234567890123456`

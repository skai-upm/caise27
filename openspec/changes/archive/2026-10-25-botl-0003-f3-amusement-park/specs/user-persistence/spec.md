# Spec Delta

## MODIFIED Requirements

### Requirement: Users stored in a text file
Users SHALL be stored in a text file, one user per line, with the attributes first name, surname(s), DNI, email, password hash, telephone and card separated by `;` in that order. The password SHALL be stored as a BCrypt hash, never in plaintext. No database or external persistence library SHALL be used.

#### Scenario: File content after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash of `Clave1!`, then `;612345678;1234567890123456`

# Spec Delta

## MODIFIED Requirements

### Requirement: Users stored in a text file
Users SHALL be stored persistently in a text file, one user per line, with the attributes first name, surname(s), DNI, email, password hash, telephone and card number in that order separated by `;`. No database or external persistence library SHALL be used.

#### Scenario: File content after creation
- **WHEN** user Ana is created with the example data
- **THEN** the users file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash and `;612345678;1234567890123456`

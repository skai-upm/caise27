# Spec Delta

## MODIFIED Requirements

### Requirement: Text file user storage format
Users SHALL be stored in a text file, one user per line, with attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card number, where the password field holds the BCrypt hash of the password and never the original password (for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$10$...;612345678;1234567890123456`).

#### Scenario: Stored line format
- **WHEN** the user Ana is created and storage is written
- **THEN** the storage file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash and `;612345678;1234567890123456`, and it does not contain `Clave1!`

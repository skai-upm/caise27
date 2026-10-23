# Spec Delta

## MODIFIED Requirements

### Requirement: Text file storage format
Users SHALL be stored in a text file (`users.txt` in the working directory by default) with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, BCrypt password hash, phone, card. The plain-text password SHALL NOT appear in the file.

#### Scenario: File contents after creation
- **WHEN** Ana and then Pedro are created successfully
- **THEN** the file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending `;612345678;1234567890123456`, followed by the equivalent line for Pedro, and does not contain `Clave1!` or `Otra2#`

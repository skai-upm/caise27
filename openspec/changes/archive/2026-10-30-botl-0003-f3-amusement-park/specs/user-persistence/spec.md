# Spec Delta

## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in the text file `users.txt` in the data directory (the working directory in normal mode, the temporary directory in `--fly` mode), one user per line, with attributes in the order first name, surname(s), DNI, email, BCrypt password hash, phone, card, separated by `;`.

#### Scenario: Stored line format
- **WHEN** user Ana (`Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is created
- **THEN** the file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending `;612345678;1234567890123456`, and does not contain `Clave1!`

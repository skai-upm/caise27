## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file with one user per line, attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card number. No database or external persistence library SHALL be used.

#### Scenario: Stored line format
- **WHEN** the user Ana Garcia Lopez (`12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is stored
- **THEN** the file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is the BCrypt hash of `Clave1!`

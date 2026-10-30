## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file, one user per line, with attributes in the order first name, surnames, DNI, email, BCrypt password hash, phone, card separated by `;`.

#### Scenario: Stored line format
- **WHEN** the user Ana Garcia Lopez is created with password `Clave1!`
- **THEN** the file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

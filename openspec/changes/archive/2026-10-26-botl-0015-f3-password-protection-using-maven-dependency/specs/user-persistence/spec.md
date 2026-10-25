# Spec Delta

## MODIFIED Requirements

### Requirement: Users file format
Users SHALL be stored in a text file (`data/users.txt` by default), one user per line, with the attributes in the order first name, surname(s), DNI, email, password hash, telephone, card number, separated by `;`. The password column holds the BCrypt hash of the password, never the plaintext.

#### Scenario: File content after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash of `Clave1!`

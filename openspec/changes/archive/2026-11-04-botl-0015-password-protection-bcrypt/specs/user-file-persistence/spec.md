# Spec Delta

## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in a UTF-8 text file with one user per line and the attributes in the order first name, surname(s), DNI, email, password, phone, card number, separated by `;`. The password field SHALL contain the BCrypt hash of the password, never the plaintext. No database or external persistence library SHALL be used.

#### Scenario: File content after a creation
- **WHEN** the user from `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is created
- **THEN** the file contains one line whose fields are `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, a BCrypt hash starting with `$2a$` that verifies `Clave1!`, `612345678` and `1234567890123456`, and the text `Clave1!` does not appear in the file

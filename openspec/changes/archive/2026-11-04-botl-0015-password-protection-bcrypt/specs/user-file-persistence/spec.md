# Spec Delta

## MODIFIED Requirements

### Requirement: Text file format
The system SHALL store users in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, BCrypt hash of the password, telephone, card number. The plaintext password SHALL NOT be written.

#### Scenario: File contents after creation
- **WHEN** user Ana is created with the appendix example data
- **THEN** the file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is a BCrypt hash (starting with `$2a$`) that verifies `Clave1!`, and the text `Clave1!` does not appear in the file

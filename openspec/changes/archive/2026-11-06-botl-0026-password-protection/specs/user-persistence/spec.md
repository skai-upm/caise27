# Spec Delta

## MODIFIED Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and the attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number; every attribute is stored exactly as supplied except the password, which is stored as its BCrypt hash.

#### Scenario: Created user is written as one line
- **GIVEN** an empty user file
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the file contains exactly one line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456` where `<hash>` is the BCrypt hash of `Clave1!`

#### Scenario: Rejected command does not change the file
- **GIVEN** a user file containing user `12345678Z`
- **WHEN** a `USER CREATE` command with a duplicate DNI is rejected
- **THEN** the file content is unchanged

# Spec Delta

## MODIFIED Requirements

### Requirement: Text file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card, for example `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`; from Delivery 3 the password column SHALL hold the BCrypt hash of the password instead of the password itself. (F1-C04, F3-C05)

#### Scenario: Stored line format
- **WHEN** the user Ana is created successfully
- **THEN** the users file contains a line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<bcrypt-hash>;612345678;1234567890123456` whose fifth field is a BCrypt hash of `Clave1!`

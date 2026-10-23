# Spec Delta

## MODIFIED Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password hash, phone, card, where the password hash is a BCrypt value and the original password never appears in the file.

#### Scenario: Stored line
- **WHEN** the user Ana Garcia Lopez is created with the example data
- **THEN** the users file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending `;612345678;1234567890123456`, and does not contain `Clave1!`

#### Scenario: Password change stores a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes, `Nueva2#` does not appear in the file, and the new password authenticates after a restart

# Spec Delta

## MODIFIED Requirements

### Requirement: Users file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card number. The password field SHALL contain only the BCrypt hash, never the original password.

#### Scenario: File line format
- **WHEN** the user Ana Garcia Lopez is created with password `Clave1!`
- **THEN** the users file contains a line beginning `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$`, ending `;612345678;1234567890123456`, and not containing `Clave1!`

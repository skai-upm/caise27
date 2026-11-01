# Spec Delta

## MODIFIED Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card number. The password column SHALL hold the BCrypt hash, never the plaintext. No database or external persistence library SHALL be used.

#### Scenario: File format
- **GIVEN** user Ana Garcia Lopez with DNI `12345678Z` and password `Clave1!` has been created
- **WHEN** the user file is read
- **THEN** it contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending `;612345678;1234567890123456`, without `Clave1!`

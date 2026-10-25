# Spec Delta

## MODIFIED Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order: first name, surname(s), DNI, email, password hash, telephone, card number. The password field SHALL contain a BCrypt hash, never the plaintext password.

#### Scenario: File content after creation
- **WHEN** user Ana is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the users file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2` followed by the rest of the BCrypt hash and `;612345678;1234567890123456`, and does not contain `Clave1!`

## ADDED Requirements

### Requirement: Legacy plaintext migration
When loading, a stored password that is not a BCrypt hash SHALL be treated as a legacy plaintext password, replaced by its hash, and the users file SHALL be rewritten.

#### Scenario: Delivery 2 users file
- **WHEN** the users file contains a user with plaintext password `Clave1!` and the application starts
- **THEN** the user can authenticate with `Clave1!` and the file now stores a hash instead of the plaintext

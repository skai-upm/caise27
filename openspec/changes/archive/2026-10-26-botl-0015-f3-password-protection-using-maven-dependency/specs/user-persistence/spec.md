# Spec Delta

## MODIFIED Requirements

### Requirement: Semicolon-separated user file
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, phone, card number. Plain-text passwords SHALL NOT be written to the file.

#### Scenario: File content after creation
- **WHEN** user Ana (`Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678`, `1234567890123456`) is created
- **THEN** the file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash (starting with `$2a$`) and `;612345678;1234567890123456`, and does not contain `Clave1!`

## ADDED Requirements

### Requirement: Migration of plain-text passwords
When users are loaded, any stored password that is not a BCrypt hash SHALL be treated as a legacy plain-text password: it SHALL be replaced by its hash and the user file SHALL be rewritten without plain-text passwords.

#### Scenario: Legacy file
- **WHEN** the user file contains `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` and the application starts
- **THEN** Ana can still authenticate with `Clave1!` and the file no longer contains `Clave1!`

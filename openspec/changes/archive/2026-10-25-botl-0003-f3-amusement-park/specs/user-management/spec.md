# Spec Delta

## MODIFIED Requirements

### Requirement: User persistence
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, telephone, card. The password field SHALL hold the BCrypt hash, never the plaintext password. The file SHALL be rewritten after every successful create, delete or update, and users stored previously SHALL be loaded when the application starts. No database or external persistence library SHALL be used.

#### Scenario: Users survive a restart
- **WHEN** two users are created, the application is stopped and started again
- **THEN** both users exist with the same data, and creating another user with one of their DNIs or emails is rejected

#### Scenario: File format
- **WHEN** user Ana from the creation example is stored
- **THEN** the storage file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;`, followed by a BCrypt hash of `Clave1!` and `;612345678;1234567890123456`

#### Scenario: Malformed stored line
- **WHEN** the storage file contains a line that does not have seven fields
- **THEN** that line is skipped with a warning and the remaining users are loaded

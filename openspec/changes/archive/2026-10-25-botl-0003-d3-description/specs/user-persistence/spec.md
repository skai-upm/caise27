# Spec Delta

## MODIFIED Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password hash, phone, card. The password field SHALL contain only a BCrypt hash of the password, never the password itself.

#### Scenario: Stored line format
- **WHEN** the user Ana from the creation example is created
- **THEN** the user file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$` and ending with `;612345678;1234567890123456`, and does not contain `Clave1!`

## ADDED Requirements

### Requirement: Password verification with BCrypt
Operations that require a password (`USER DELETE`, `USER UPDATE`) SHALL verify it against the stored BCrypt hash. Password rules SHALL be checked before hashing, and a changed password SHALL be stored as a new hash. Plaintext user files from earlier deliveries need not be migrated.

#### Scenario: Correct password after restart
- **WHEN** Ana is created, the application restarts, and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted

#### Scenario: Password change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds
- **THEN** the stored hash changes, `Nueva2#` is accepted and `Clave1!` is rejected afterwards

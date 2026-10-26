# Spec Delta

## MODIFIED Requirements

### Requirement: User data
Each user SHALL store first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created. The password SHALL be stored only as a BCrypt hash.

#### Scenario: Stored attributes
- **WHEN** a user is created with `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **THEN** the stored user has those values for name, surname(s), DNI, email, telephone and card, and a BCrypt hash that matches `Clave1!` as password

### Requirement: Persistent user storage
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surname(s), DNI, email, password hash, phone, card. Users stored in the file SHALL be loaded when the application starts.

#### Scenario: Users survive a restart
- **WHEN** a user is created, the application terminates and is started again
- **THEN** the previously created user exists and can be updated or deleted with the original password

#### Scenario: File format
- **WHEN** Ana is created with the example data
- **THEN** the file contains a line starting with `Ana;Garcia Lopez;12345678Z;ana@correo.com;$2` (a BCrypt hash) and ending with `;612345678;1234567890123456`, and does not contain `Clave1!`

## ADDED Requirements

### Requirement: Passwords stored as BCrypt hashes
New passwords SHALL be validated with the password rules and then stored as BCrypt hashes. Changing a password SHALL persist a new hash. Operations requiring authentication (`USER DELETE`, `USER UPDATE`) SHALL verify the supplied password against the stored hash.

#### Scenario: Delete with correct and wrong password
- **WHEN** Ana was created with `Clave1!` and `USER DELETE;ana@correo.com;Otra1!` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** the first is rejected and the second deletes Ana

#### Scenario: Password change persists a new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored hash changes, matches `Nueva2#`, no longer matches `Clave1!`, and the file contains no plaintext password

#### Scenario: Invalid new password is not hashed
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave` is processed
- **THEN** the update is rejected by the password rules and the stored hash is unchanged

### Requirement: Legacy plaintext passwords are migrated
When users are loaded and a stored password is not a BCrypt hash, the system SHALL replace it with its hash and save the users, so no plaintext password remains stored.

#### Scenario: Old data file
- **WHEN** `users.txt` contains `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` and the application starts
- **THEN** the file is rewritten with a hash instead of `Clave1!` and Ana can authenticate with `Clave1!`

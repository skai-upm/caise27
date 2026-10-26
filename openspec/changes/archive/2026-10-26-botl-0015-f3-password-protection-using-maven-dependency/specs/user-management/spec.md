# Spec Delta

## MODIFIED Requirements

### Requirement: User persistence
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password hash, telephone, card. The password SHALL be stored only as its BCrypt hash, never in plaintext. After every successful create, delete or update the file SHALL reflect the current users. At startup, previously stored users SHALL be loaded; a missing file means no users.

#### Scenario: Users survive restart
- **WHEN** a user is created, the application terminates, and the application is started again
- **THEN** the user exists and can be deleted or updated with its stored credentials

#### Scenario: File format
- **WHEN** user Ana is created with the example data
- **THEN** the file contains a line starting `Ana;Garcia Lopez;12345678Z;ana@correo.com;` followed by a BCrypt hash (starting `$2a$`) and `;612345678;1234567890123456`, and does not contain `Clave1!`

#### Scenario: First execution
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without error

## ADDED Requirements

### Requirement: Password hashing
New passwords SHALL first pass the Delivery 1 format rules and SHALL then be stored as BCrypt hashes computed with the external BCrypt library. Operations requiring authentication (`USER DELETE`, `USER UPDATE`) SHALL verify the supplied password against the stored hash.

#### Scenario: Delete with correct password
- **WHEN** Ana was created with `Clave1!` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the password matches the stored hash and Ana is deleted

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Mala1!` or `USER UPDATE;ana@correo.com;Mala1!;NAME;X` is processed
- **THEN** the hash does not match and the operation is rejected with an `ERROR` line

#### Scenario: Format rules still apply
- **WHEN** `USER CREATE` or a password update uses `clave1!`
- **THEN** it is rejected for not meeting the format rules and nothing is hashed or stored

### Requirement: Password change persists a new hash
Changing the password with `USER UPDATE;Correo;Contraseña;PASSWORD;NuevoValor` SHALL verify the current password, validate the new one, and persist the BCrypt hash of the new password, after which only the new password authenticates.

#### Scenario: Change password
- **WHEN** Ana changes her password from `Clave1!` to `Nueva2#`
- **THEN** the stored hash changes, `Nueva2#` authenticates and `Clave1!` no longer does, also after a restart

### Requirement: Legacy plaintext passwords migrated
When stored users are loaded and a password is not a BCrypt hash (data written by earlier deliveries), it SHALL be replaced by its hash and the file rewritten, so the user keeps authenticating with the same password and no plaintext password remains stored.

#### Scenario: Old data file
- **WHEN** the users file contains `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` and the application starts
- **THEN** the file is rewritten with a BCrypt hash instead of `Clave1!` and `USER DELETE;ana@correo.com;Clave1!` succeeds

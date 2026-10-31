# user-management Specification

## Purpose

Defines the registered-user data model, the validation and uniqueness rules applied to user data, and the user creation, deletion and update operations.

## Requirements

### Requirement: User data model
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL be immutable once the user has been created.

#### Scenario: Created user holds all attributes
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed with no conflicting user
- **THEN** a user exists with first name `Ana`, surname(s) `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`

### Requirement: User creation command
The system SHALL accept `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` with parameters in that order. It SHALL validate all data before creating the user and SHALL NOT store any user when any value is invalid or a parameter is missing.

#### Scenario: Valid registration
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user has DNI `12345678Z` or email `ana@correo.com`
- **THEN** the user is created and stored

#### Scenario: Missing parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** no user is created and an error message is reported

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter, and the letter SHALL match the standard Spanish DNI algorithm (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). Format-only checks SHALL NOT suffice.

#### Scenario: Valid checksum letter
- **WHEN** a user is created with DNI `12345678Z`
- **THEN** the DNI is accepted

#### Scenario: Wrong checksum letter
- **WHEN** an otherwise valid user is created with DNI `12345678A`
- **THEN** creation is rejected

#### Scenario: Wrong shape
- **WHEN** an otherwise valid user is created with DNI `1234567Z` or `123456789`
- **THEN** creation is rejected

### Requirement: Email validation
An email SHALL follow `texto@texto.xxx`: exactly one `@`, a non-empty part before it, a part after it containing a period, a non-empty domain before the last period, and an extension of exactly three characters after the last period. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** a user is created with email `ana@correo.com`
- **THEN** the email is accepted

#### Scenario: Invalid emails
- **WHEN** an otherwise valid user is created with email `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es`
- **THEN** creation is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or third-party libraries.

#### Scenario: Valid password
- **WHEN** a user is created with password `Clave1!`
- **THEN** the password is accepted

#### Scenario: Missing character category
- **WHEN** an otherwise valid user is created with password `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12`
- **THEN** creation is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** a user is created with card `1234567890123456`
- **THEN** the card is accepted

#### Scenario: Invalid cards
- **WHEN** an otherwise valid user is created with card `1234 5678 9012 3456`, `123456789012345` or `12345678901234567`
- **THEN** creation is rejected

### Requirement: Telephone stored as given
The telephone number SHALL be stored exactly as supplied, without transformation or encryption.

#### Scenario: Telephone unchanged
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is `612345678`

### Requirement: User uniqueness
No two registered users SHALL share a DNI, and no two registered users SHALL share an email address. Both constraints SHALL be checked before a user is created.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: User deletion command
The system SHALL accept `USER DELETE;Correo;Contraseña`. It SHALL find the user by email, check the supplied password matches the stored password, and only then delete the user and update persistent storage. Deletion SHALL be rejected while the user holds any ticket (individual or group constituent) dated on or after the system date; past tickets alone SHALL NOT block it. A deleted user SHALL be removed from every group it belonged to.

#### Scenario: Authenticated deletion
- **WHEN** a user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the persistent store no longer contains it

#### Scenario: Wrong password or unknown email on delete
- **WHEN** `USER DELETE` is processed with an incorrect password or an email that is not registered
- **THEN** no user is deleted and an error message is reported

#### Scenario: Current or future ticket blocks deletion
- **WHEN** `SYSTEM DATE;2026-10-01` is set, `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** deletion is rejected and Ana and `T001` remain

#### Scenario: Historical ticket does not block deletion
- **WHEN** the system date is then changed to `2026-10-16` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and `T001` remains as a historical record

#### Scenario: Deleted user leaves groups
- **WHEN** Ana is a member of an unlocked group `G01` and is deleted
- **THEN** `G01` no longer contains Ana, including after a restart

### Requirement: User update command
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, where `Campo` is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD`. It SHALL verify the current email and password before any modification and SHALL persist a successful change. The DNI SHALL NOT be updatable.

#### Scenario: Update first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** the user's first name becomes `Ana Maria` and the change is persisted

#### Scenario: Chained email, password and card updates
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** each update succeeds and the user ends with email `ana.nueva@correo.com`, password `Nueva2#` and card `9999888877776666`

#### Scenario: Wrong credentials on update
- **WHEN** `USER UPDATE` is processed with an incorrect password or unregistered email
- **THEN** no user is modified and an error message is reported

#### Scenario: DNI is not editable
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the update is rejected and the DNI is unchanged

#### Scenario: Unknown field
- **WHEN** `USER UPDATE` is processed with a field name outside the supported set
- **THEN** the update is rejected and no user is modified

### Requirement: Update revalidation
A new value supplied through `USER UPDATE` SHALL satisfy the same validation as at creation: a new email SHALL satisfy the email format and SHALL NOT belong to another user; a new password SHALL satisfy the password rules; a new card SHALL satisfy the 16-digit rule. On rejection the user's state SHALL remain unchanged.

#### Scenario: Email taken by another user
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and Ana updates her email to `pedro@correo.com`
- **THEN** the update is rejected and both users are unchanged

#### Scenario: Invalid new password or card
- **WHEN** an authenticated update supplies password `clave1!` or card `123456789012345`
- **THEN** the update is rejected and the original values remain

### Requirement: Password protection
Passwords SHALL be validated with the E1 password rules and then stored only as BCrypt hashes, both at `USER CREATE` and when `USER UPDATE` changes the `PASSWORD` field (which SHALL store a new hash). The BCrypt library SHALL be declared as a Maven dependency.

#### Scenario: No plaintext stored
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the stored user record contains a BCrypt hash and does not contain `Clave1!`

#### Scenario: Password change stores new hash
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` succeeds and the application restarts
- **THEN** the stored hash differs from the previous one and `Nueva2#` authenticates while `Clave1!` does not

#### Scenario: Format rules still apply
- **WHEN** a user is created or updated with password `clave1!`
- **THEN** the operation is rejected

### Requirement: Password verification
`USER DELETE` and `USER UPDATE` SHALL verify the supplied password against the stored BCrypt hash. A stored value that is not a BCrypt hash (written by an earlier delivery) SHALL be compared as legacy plaintext, without automatic migration.

#### Scenario: Correct and incorrect passwords
- **WHEN** `USER DELETE` or `USER UPDATE` is executed with the correct password and then with a wrong one
- **THEN** the correct password is accepted and the wrong one rejected

#### Scenario: Legacy plaintext user
- **WHEN** `users.txt` contains a user written by E1 with plaintext password `Clave1!`
- **THEN** that user can still authenticate with `Clave1!`

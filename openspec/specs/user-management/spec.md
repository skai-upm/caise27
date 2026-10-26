# user-management Specification

## Purpose


Defines how park users are registered, deleted and updated through commands, including validation of personal data, uniqueness by DNI and email, DNI immutability, and persistence of users between executions.

## Requirements

### Requirement: User data
Each user SHALL store first name, surname(s), DNI, email address, password, telephone number and card number. All seven values are required and none may be blank.

#### Scenario: Blank value rejected
- **WHEN** `USER CREATE` is received with an empty first name
- **THEN** the user is not created and an `ERROR` line is printed

### Requirement: Create user
The command `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` SHALL create and persist a user only when every value is valid and the DNI and email are not already registered; otherwise no user is stored and an `ERROR` line names the failing check.

#### Scenario: Valid user created
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is received and no user has that DNI or email
- **THEN** the user is stored and `OK USER CREATED` is printed

#### Scenario: Invalid data not stored
- **WHEN** `USER CREATE` is received with any invalid DNI, email, password or card
- **THEN** no user is stored and an `ERROR` line is printed

### Requirement: DNI validation
A DNI SHALL be exactly eight digits followed by one letter, and the letter SHALL equal the standard Spanish check letter for those digits (the digits modulo 23 indexing `TRWAGMYFPDXBNJZSQVHLCKE`). The letter is compared case-insensitively and stored in upper case.

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong format
- **WHEN** a DNI with fewer than eight digits or without a final letter is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL have the form `texto@texto.xxx`: exactly one `@`, a non-empty part before it, a domain after it that contains a period, a non-empty domain name before the last period, and an extension after the last period of exactly three characters. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive decimal digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone number storage
The telephone number SHALL be stored as received, without format validation, encryption or external processing.

#### Scenario: Telephone stored verbatim
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored user has telephone `612345678`

### Requirement: User uniqueness
No two users SHALL share the same DNI, and no two users SHALL share the same email address (compared case-insensitively). Both checks SHALL be made before creating a user.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user `12345678Z;ana@correo.com` exists and `USER CREATE` is received with DNI `12345678Z` and email `otro@correo.com`
- **THEN** the user is not created and an `ERROR` line is printed

#### Scenario: Duplicate email rejected
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is received with DNI `87654321X` and email `ana@correo.com`
- **THEN** the user is not created and an `ERROR` line is printed

### Requirement: Delete user
The command `USER DELETE;Correo;Contraseña` SHALL delete the user with that email only when the user exists, the password matches the stored password, and the user has no ticket dated on or after the system date; it SHALL update the persistent file. When the deletion succeeds, the user's remaining (past) tickets SHALL also be removed. Otherwise no user is deleted and an `ERROR` line is printed.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is received and that user exists with password `Clave1!`
- **THEN** the user is removed from the system and the file, and `OK USER DELETED` is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is received with an email that is not registered
- **THEN** no user is deleted and an `ERROR` line is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is received with a registered email and an incorrect password
- **THEN** no user is deleted and an `ERROR` line is printed

#### Scenario: Future ticket blocks deletion
- **WHEN** the system date is 2026-10-01, Ana has ticket `T001` dated 2026-10-15, and `USER DELETE;ana@correo.com;Clave1!` is received
- **THEN** the deletion is rejected with an `ERROR` line and Ana remains registered

#### Scenario: Ticket for today blocks deletion
- **WHEN** the system date is 2026-10-15 and Ana has a ticket dated 2026-10-15
- **THEN** `USER DELETE` for Ana is rejected with an `ERROR` line

#### Scenario: Past ticket does not block deletion
- **WHEN** the system date is 2026-10-16 and Ana's only ticket is dated 2026-10-15
- **THEN** `USER DELETE` for Ana succeeds and her past ticket is removed

### Requirement: Update user
The command `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` SHALL first verify that the user exists and the password matches, then validate the new value for the field, apply the change and update the persistent file. Editable fields are `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD` (matched case-insensitively). On any failure no data changes and an `ERROR` line is printed.

#### Scenario: Update first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is received with correct credentials
- **THEN** the first name becomes `Ana Maria`, the file reflects it, and `OK USER UPDATED` is printed

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is received with correct credentials and the new email is valid and unused
- **THEN** the user's email becomes `ana.nueva@correo.com`

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is received with an unknown email or an incorrect password
- **THEN** no data changes and an `ERROR` line is printed

#### Scenario: Invalid new value
- **WHEN** the new value is an invalid email, a password not meeting the rules, or a card that is not 16 digits
- **THEN** no data changes and an `ERROR` line is printed

#### Scenario: Email taken by another user
- **WHEN** the new email belongs to another user
- **THEN** no data changes and an `ERROR` line is printed

#### Scenario: Unknown field
- **WHEN** the field is not one of the editable fields
- **THEN** no data changes and an `ERROR` line is printed

### Requirement: DNI immutability
The DNI of a user SHALL NOT change after creation; `USER UPDATE` with field `DNI` SHALL be rejected.

#### Scenario: DNI update rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is received
- **THEN** the DNI is unchanged and an `ERROR` line states that the DNI cannot be modified

### Requirement: User persistence
Users SHALL be stored in a text file with one user per line and attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card. After every successful create, delete or update the file SHALL reflect the current users. At startup, previously stored users SHALL be loaded; a missing file means no users.

#### Scenario: Users survive restart
- **WHEN** a user is created, the application terminates, and the application is started again
- **THEN** the user exists and can be deleted or updated with its stored credentials

#### Scenario: File format
- **WHEN** user Ana is created with the example data
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

#### Scenario: First execution
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without error

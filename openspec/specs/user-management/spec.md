# user-management Specification

## Purpose

Manages registered park users: their data, validation rules, identifier uniqueness, and creation, deletion and update through text commands.

## Requirements

### Requirement: User data
Each user SHALL hold first name, surname(s), DNI, email address, password, telephone number and card number. User state SHALL be encapsulated and the DNI SHALL be immutable once the user exists.

#### Scenario: DNI cannot change
- **GIVEN** an existing user
- **WHEN** any update is requested for that user
- **THEN** the user's DNI remains unchanged

### Requirement: Separated responsibilities
User representation, application start-up/terminal interaction, and saving/loading of users SHALL be handled by distinct classes. Neither the user representation nor the main application class SHALL read or write the user file directly.

#### Scenario: Responsibility separation
- **WHEN** the implementation is inspected
- **THEN** user representation, application entry/interaction and file persistence live in distinct classes

### Requirement: Create user command
The system SHALL accept `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` and SHALL store the user only if every field is valid and the DNI and email are not already registered. A rejected creation SHALL store nothing.

#### Scenario: Valid registration
- **GIVEN** no user has DNI `12345678Z` or email `ana@correo.com`
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is created and persisted

#### Scenario: Missing parameters
- **WHEN** `USER CREATE` is processed with fewer than seven parameters
- **THEN** the command is rejected with an error message and no user is stored

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one letter that matches the standard Spanish DNI checksum (digits modulo 23 indexing `TRWAGMYFPDXBNJZSQVHLCKE`). Format-only checks SHALL NOT suffice.

#### Scenario: Correct checksum letter
- **WHEN** a user is created with DNI `12345678Z`
- **THEN** the DNI is accepted

#### Scenario: Wrong checksum letter
- **WHEN** an otherwise valid user is created with DNI `12345678A`
- **THEN** creation is rejected

### Requirement: Email validation
An email SHALL match `texto@texto.xxx`: a nonempty part before `@`, a nonempty domain after `@` containing a period, and an extension after the last period of exactly three characters. Validation SHALL use basic string operations, not regular expressions.

#### Scenario: Valid email
- **WHEN** a user is created with email `ana@correo.com`
- **THEN** the email is accepted

#### Scenario: Invalid emails
- **WHEN** a user is created with any of `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es`
- **THEN** creation is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace), checked without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** a user is created with password `Clave1!`
- **THEN** the password is accepted

#### Scenario: Missing character category
- **WHEN** a user is created with any of `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12`
- **THEN** creation is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** a user is created with card `1234567890123456`
- **THEN** the card is accepted

#### Scenario: Invalid cards
- **WHEN** a user is created with any of `1234 5678 9012 3456`, `123456789012345` or `12345678901234567`
- **THEN** creation is rejected

### Requirement: Telephone stored as given
The telephone number SHALL be stored exactly as supplied, without validation, transformation or encryption.

#### Scenario: Telephone preserved
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored user's telephone is `612345678`

### Requirement: Unique DNI and email
No two registered users SHALL share a DNI or an email address; both SHALL be checked before a user is created.

#### Scenario: Duplicate DNI
- **GIVEN** a user with DNI `12345678Z` and email `ana@correo.com` exists
- **WHEN** a user is created with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **GIVEN** a user with DNI `12345678Z` and email `ana@correo.com` exists
- **WHEN** a user is created with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: Delete user command
The system SHALL accept `USER DELETE;Correo;Contraseña`, locate the user by email, and delete it only if the password matches the stored password, then update persistent storage. An unknown email or wrong password SHALL leave all users unchanged. Deletion SHALL also be rejected while the user holds any ticket (individual or constituent of a group ticket) dated on or after the system date, or belongs to a group locked by a group ticket. A deleted user SHALL be removed from every group and attraction; their historical tickets SHALL be retained.

#### Scenario: Authenticated deletion
- **GIVEN** user `ana@correo.com` with password `Clave1!` exists
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and the user file no longer contains it

#### Scenario: Wrong credentials on delete
- **WHEN** `USER DELETE` is processed with a nonexistent email or an incorrect password
- **THEN** no user is deleted

#### Scenario: Current or future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and ticket `T001` for `ana@correo.com` dated `2026-10-15`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** deletion is rejected and both the user and the ticket remain

#### Scenario: Ticket dated today blocks deletion
- **GIVEN** the system date equals the date of a ticket held by the user
- **WHEN** the user is deleted
- **THEN** deletion is rejected

#### Scenario: Historical ticket does not block deletion
- **GIVEN** ticket `T001` for `ana@correo.com` dated `2026-10-15` and the system date changed to `2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted and ticket `T001` is still retained as a historical record, also after a restart

#### Scenario: Deleted user leaves groups
- **GIVEN** an unlocked group containing the user
- **WHEN** the user is deleted
- **THEN** the group no longer lists the user, also after a restart

### Requirement: Update user command
The system SHALL accept `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, verify the current email and password before any change, and update one field. Editable fields SHALL be `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`; DNI SHALL NOT be editable. Successful updates SHALL be persisted.

#### Scenario: Update first name
- **GIVEN** user `ana@correo.com` with password `Clave1!` exists
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the user's first name becomes `Ana Maria` and the change is persisted

#### Scenario: Wrong credentials on update
- **WHEN** `USER UPDATE` is processed with a nonexistent email or an incorrect password
- **THEN** no user data changes

#### Scenario: Unknown or non-editable field
- **WHEN** `USER UPDATE` names a field that is not one of the editable fields, such as `DNI`
- **THEN** the update is rejected and no user data changes

### Requirement: Update values revalidated
A new value for a restricted field SHALL pass the same validation as at creation: email format and uniqueness against other users, password character rules, and the 16-digit card rule. A rejected update SHALL leave the original state intact.

#### Scenario: Email taken by another user
- **GIVEN** two registered users
- **WHEN** one user updates their email to the other user's email
- **THEN** the update is rejected

#### Scenario: Invalid password or card on update
- **GIVEN** a user with valid current credentials
- **WHEN** an update supplies a password failing the character rules or a card failing the 16-digit rule
- **THEN** the update is rejected and the stored user is unchanged

# user-management Specification

## Purpose

Defines the user model and the USER CREATE, USER DELETE and USER UPDATE commands, including validation of user data and uniqueness of DNI and email.

## Requirements

### Requirement: User data
Each user SHALL store first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created.

#### Scenario: DNI is immutable
- **WHEN** any update is attempted on a user's DNI
- **THEN** the DNI keeps the value given at creation

### Requirement: Create user
The command `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` SHALL create a user after validating all data. If any value is invalid or empty, the user SHALL NOT be created and an error SHALL be reported.

#### Scenario: Valid creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed and no user with that DNI or email exists
- **THEN** the user is created and stored, and an `OK` result is printed

#### Scenario: Invalid data rejects creation
- **WHEN** a `USER CREATE` command has an invalid DNI, email, password or card number
- **THEN** no user is created and an `ERROR` result describing the invalid field is printed

#### Scenario: Empty field rejects creation
- **WHEN** a `USER CREATE` command has an empty first name, surname or telephone
- **THEN** no user is created and an `ERROR` result is printed

### Requirement: DNI validation
A DNI SHALL be valid only if it has exactly eight digits followed by one letter, and the letter matches the standard Spanish check-letter algorithm (number modulo 23 indexed into `TRWAGMYFPDXBNJZSQVHLCKE`). The letter comparison SHALL be case-insensitive.

#### Scenario: Valid DNI
- **WHEN** DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong format
- **WHEN** DNI `1234567Z` or `123456789` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email SHALL be valid only if it has the form `texto@texto.xxx`: exactly one `@`, a non-empty part before `@`, a part after `@` containing a period, a non-empty domain before the last period, and an extension of exactly three characters after the last period. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** email `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is not a letter, digit or whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** password `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** card `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: Telephone number storage
The telephone number SHALL only be required to be non-empty and SHALL be stored as given, without encryption or further processing.

#### Scenario: Telephone stored as given
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is `612345678`

### Requirement: User uniqueness
The system SHALL reject creating a user whose DNI or email already belongs to an existing user. Email comparison SHALL be case-insensitive and DNI comparison SHALL ignore letter case.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` and email `ana@correo.com` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** the user is not created and an `ERROR` result is printed

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** the user is not created and an `ERROR` result is printed

### Requirement: Delete user
The command `USER DELETE;Correo;Contraseña` SHALL delete the user with that email only if the user exists and the supplied password matches the stored password; the stored data SHALL then be updated.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and the user exists with that password
- **THEN** the user is deleted, the stored data no longer contains it, and an `OK` result is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed with an email that does not belong to any user
- **THEN** no user is deleted and an `ERROR` result is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and a wrong password
- **THEN** the user is not deleted and an `ERROR` result is printed

### Requirement: Update user
The command `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` SHALL update one field of the user identified by the current email after verifying the current password. `Campo` SHALL be one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (case-insensitive). After a successful update the stored data SHALL reflect the new state.

#### Scenario: Update first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** the user's first name becomes `Ana Maria` and the change is stored

#### Scenario: Update email then password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` and then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#` are processed
- **THEN** the user is identified by the new email and its password becomes `Nueva2#`

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with an unknown email or a wrong password
- **THEN** no data is changed and an `ERROR` result is printed

#### Scenario: Unknown or non-editable field
- **WHEN** `USER UPDATE` is processed with field `DNI` or any value not in the editable list
- **THEN** no data is changed and an `ERROR` result is printed

### Requirement: Validation of updated values
When an updated field has restrictions, the new value SHALL be validated with the same rules as on creation: a new email must be valid and not belong to another user, a new password must meet the password rules, and a new card number must have exactly 16 digits. Empty new values SHALL be rejected.

#### Scenario: New email already used
- **WHEN** a user tries to change its email to one belonging to another user
- **THEN** no data is changed and an `ERROR` result is printed

#### Scenario: Invalid new password
- **WHEN** a user tries to change its password to `clave1!`
- **THEN** no data is changed and an `ERROR` result is printed

#### Scenario: Invalid new card
- **WHEN** a user tries to change its card to `123456789012345`
- **THEN** no data is changed and an `ERROR` result is printed

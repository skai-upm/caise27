# Spec Delta

## Purpose

Defines park users: their data, the validation rules applied to that data, uniqueness constraints, and the operations to create, delete and update users.

## ADDED Requirements

### Requirement: User data
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL be immutable once the user is created. First name, surname(s) and telephone SHALL be non-empty; the telephone is only stored, without format validation or encryption.

#### Scenario: Empty name rejected
- **WHEN** `USER CREATE` is processed with an empty first name
- **THEN** the response is `ERROR EMPTY FIELD` and no user is created

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one uppercase letter, and the letter SHALL equal the standard Spanish check letter, i.e. the character at position (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`. Checking only the format is not sufficient.

#### Scenario: Valid DNI
- **WHEN** a user is created with DNI `12345678Z`
- **THEN** the DNI is accepted

#### Scenario: Wrong check letter
- **WHEN** a user is created with DNI `12345678A`
- **THEN** the response is `ERROR INVALID DNI`

#### Scenario: Wrong format
- **WHEN** a user is created with DNI `1234567Z` or `123456789`
- **THEN** the response is `ERROR INVALID DNI`

### Requirement: Email validation
An email SHALL have the form `texto@texto.xxx`, checked with `String` operations and `split` and without regular expressions: exactly one `@`, a non-empty part before it, a part after it that contains a period, a non-empty domain before the last period, and an extension after the last period of exactly three characters.

#### Scenario: Valid email
- **WHEN** a user is created with email `ana@correo.com`
- **THEN** the email is accepted

#### Scenario: Invalid emails
- **WHEN** a user is created with email `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es`
- **THEN** the response is `ERROR INVALID EMAIL`

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace), checked with standard Java functionality and without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** a user is created with password `Clave1!`
- **THEN** the password is accepted

#### Scenario: Invalid passwords
- **WHEN** a user is created with password `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12`
- **THEN** the response is `ERROR INVALID PASSWORD`

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** a user is created with card `1234567890123456`
- **THEN** the card is accepted

#### Scenario: Invalid cards
- **WHEN** a user is created with card `1234 5678 9012 3456`, `123456789012345` or `12345678901234567`
- **THEN** the response is `ERROR INVALID CARD`

### Requirement: User uniqueness
Two users SHALL NOT share the same DNI, and two users SHALL NOT share the same email address (compared ignoring letter case). Both restrictions are checked before creating a user.

#### Scenario: Duplicate DNI
- **WHEN** user `12345678Z;ana@correo.com` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** the response is `ERROR DUPLICATE DNI` and no user is created

#### Scenario: Duplicate email
- **WHEN** user `12345678Z;ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** the response is `ERROR DUPLICATE EMAIL` and no user is created

### Requirement: Create user
`USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` SHALL validate every field and uniqueness before creating the user; if anything is invalid the user SHALL NOT be stored. On success the user is persisted and the response is `OK USER CREATED`.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the response is `OK USER CREATED` and the user is stored

### Requirement: Delete user
`USER DELETE;Correo;Contraseña` SHALL find the user by email, check that it exists and that the supplied password matches the stored one, delete it and update the persistent file. If the email does not exist or the password is wrong, the user SHALL NOT be deleted and the response is `ERROR INVALID CREDENTIALS`.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `OK USER DELETED` and the user no longer exists, also after restarting

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is processed for that user
- **THEN** the response is `ERROR INVALID CREDENTIALS` and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** the response is `ERROR INVALID CREDENTIALS`

### Requirement: Update user
`USER UPDATE;Correo;Contraseña;Campo;NuevoValor` SHALL first authenticate the current email and password (`ERROR INVALID CREDENTIALS` otherwise), then change one editable field identified by an enum value: `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` (matched ignoring case). The new value SHALL be validated with the rule of that field; a new email SHALL also not belong to another user. On success the change is persisted and the response is `OK USER UPDATED`.

#### Scenario: Change first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the response is `OK USER UPDATED` and the stored first name is `Ana Maria`

#### Scenario: Change email then password then card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, then `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#`, then `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** each response is `OK USER UPDATED` and the stored user reflects all three changes

#### Scenario: Invalid new value
- **WHEN** an update sets `PASSWORD` to `clave`, `EMAIL` to `ana@correo`, or `CARD` to `1234`
- **THEN** the response is the corresponding `ERROR INVALID PASSWORD`, `ERROR INVALID EMAIL` or `ERROR INVALID CARD`, and nothing changes

#### Scenario: Email of another user
- **WHEN** an update sets `EMAIL` to the email of a different existing user
- **THEN** the response is `ERROR DUPLICATE EMAIL` and nothing changes

#### Scenario: DNI cannot be changed
- **WHEN** an update uses field `DNI` or any other non-editable name
- **THEN** the response is `ERROR INVALID FIELD` and nothing changes

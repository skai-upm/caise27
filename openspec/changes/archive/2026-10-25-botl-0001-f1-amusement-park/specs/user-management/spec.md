# Spec Delta

## Purpose

Defines how park users are created, deleted and updated through commands, including data validation and uniqueness rules.

## ADDED Requirements

### Requirement: User data
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL be immutable once the user exists.

#### Scenario: User created with all data
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** a user with exactly those seven values exists and `OK` is reported

### Requirement: Create user validates all data first
`USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` SHALL validate every field before creating the user; if any field is empty or invalid the user SHALL NOT be created or stored.

#### Scenario: Invalid field rejects creation
- **WHEN** a `USER CREATE` command has a valid DNI and email but the card `123456789012345`
- **THEN** an `ERROR` is reported and no user is created

### Requirement: DNI validation
A DNI SHALL consist of exactly eight digits followed by one uppercase letter, and the letter SHALL match the Spanish DNI algorithm (the number modulo 23 indexes `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong check letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

### Requirement: Email validation without regular expressions
An email SHALL contain exactly one `@`, a non-empty part before it, and after it a domain and an extension separated by the last `.`, where the domain is non-empty and the extension is exactly three letters. Emails SHALL NOT contain spaces.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL contain at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace).

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL consist of exactly 16 consecutive digits with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` is validated
- **THEN** each is rejected

### Requirement: Telephone stored as provided
The telephone number SHALL be stored exactly as provided, only requiring it to be non-empty.

#### Scenario: Telephone kept
- **WHEN** a user is created with telephone `612345678`
- **THEN** the stored telephone is `612345678`

### Requirement: Unique DNI and email
No two users SHALL share a DNI, and no two users SHALL share an email address (compared ignoring case). Both checks SHALL be performed before creating a user.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user with `12345678Z;ana@correo.com` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** an `ERROR` is reported and no user is created

#### Scenario: Duplicate email rejected
- **WHEN** a user with `12345678Z;ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** an `ERROR` is reported and no user is created

### Requirement: Delete user with credentials
`USER DELETE;Correo;Contraseña` SHALL delete the user with that email only if the user exists and the password matches, and SHALL update the persistent storage.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user has password `Clave1!`
- **THEN** the user is deleted, the storage no longer contains it and `OK` is reported

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed
- **THEN** an `ERROR` is reported and the user is kept

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed and no such user exists
- **THEN** an `ERROR` is reported

### Requirement: Update user field
`USER UPDATE;Correo;Contraseña;Campo;NuevoValor` SHALL, after verifying the current email and password, change one field among `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`, and SHALL update the persistent storage.

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** the user's first name becomes `Ana Maria` and `OK` is reported

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with a wrong password
- **THEN** an `ERROR` is reported and nothing changes

#### Scenario: Unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an `ERROR` lists the editable fields and nothing changes

### Requirement: Updated values are revalidated
A new email SHALL satisfy the email format and SHALL NOT belong to another user; a new password SHALL satisfy the password rules; a new card SHALL have exactly 16 digits; new values SHALL NOT be empty. An invalid new value SHALL leave the user unchanged.

#### Scenario: New email of another user
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed and `pedro@correo.com` belongs to another user
- **THEN** an `ERROR` is reported and the email is unchanged

#### Scenario: Weak new password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave` is processed
- **THEN** an `ERROR` is reported and the password is unchanged

### Requirement: DNI cannot be updated
`USER UPDATE` SHALL reject any attempt to change the DNI.

#### Scenario: Update DNI rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an `ERROR` states that the DNI cannot be modified and the DNI is unchanged

# Spec Delta

## Purpose

Manages park users: their data, validation rules, uniqueness constraints, and the create, delete and update operations driven by text commands.

## ADDED Requirements

### Requirement: User data
Each user SHALL store first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created. The telephone number SHALL only be stored, without encryption or extra processing.

#### Scenario: User created with all attributes
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is executed on an empty system
- **THEN** a user exists with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`

### Requirement: Create user
`USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` SHALL create a user only if all seven parameters are present and non-empty, all validations pass and no uniqueness rule is violated. Otherwise no user SHALL be stored and an error message SHALL be shown.

#### Scenario: Valid creation
- **WHEN** a valid `USER CREATE` command is executed
- **THEN** the system reports success and the user is stored

#### Scenario: Invalid data is rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is executed
- **THEN** the system reports an error and no user is stored

#### Scenario: Missing parameters
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is executed
- **THEN** the system reports an error about insufficient parameters and keeps running

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one letter, and the letter equals the character at position `number mod 23` in `TRWAGMYFPDXBNJZSQVHLCKE`. Checking only the format SHALL NOT be sufficient.

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
An email SHALL be valid only if it has the form `texto@texto.xxx`: exactly one `@`, a non-empty part before it, a part after it containing a period, a non-empty domain before the last period and an extension of exactly three characters after the last period. Validation SHALL NOT use regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** any of `ana`, `ana@correo`, `@correo.com`, `ana@.com`, `ana@correo.es` is validated
- **THEN** it is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). Validation SHALL NOT use regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** any of `clave1!`, `CLAVE1!`, `Clave!!`, `Clave12` is validated
- **THEN** it is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** any of `1234 5678 9012 3456`, `123456789012345`, `12345678901234567` is validated
- **THEN** it is rejected

### Requirement: User uniqueness
Two users SHALL NOT share the same DNI, and two users SHALL NOT share the same email address. Both rules SHALL be checked before creating a user and the email rule SHALL also be checked when an email is updated.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is executed with DNI `12345678Z` and email `otro@correo.com`
- **THEN** creation is rejected

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is executed with DNI `87654321X` and email `ana@correo.com`
- **THEN** creation is rejected

### Requirement: Delete user
`USER DELETE;Correo;Contraseña` SHALL delete the user identified by the email only if the user exists and the supplied password matches the stored one, and the persistent file SHALL be updated. Otherwise the user SHALL NOT be deleted and an error SHALL be reported.

#### Scenario: Successful deletion
- **WHEN** user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is executed
- **THEN** the user is removed and no longer appears in the persistent file

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is executed
- **THEN** the user is not deleted and an error is reported

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is executed
- **THEN** nothing is deleted and an error is reported

### Requirement: Update user
`USER UPDATE;Correo;Contraseña;Campo;NuevoValor` SHALL first verify that the user exists and the password matches; then it SHALL change the field identified by `Campo` (one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`) to the new value, and the persistent file SHALL reflect the new state. The DNI SHALL NOT be updatable.

#### Scenario: Update first name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is executed with correct credentials
- **THEN** the user's first name becomes `Ana Maria` and the file is updated

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE;ana@correo.com;Bad1!x;NAME;X` is executed
- **THEN** no data changes and an error is reported

#### Scenario: Unknown field or DNI
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is executed
- **THEN** the update is rejected and the DNI is unchanged

### Requirement: Revalidation on update
When the updated field has restrictions, the new value SHALL be validated again: a new email SHALL satisfy the email format and SHALL NOT belong to another user, a new password SHALL satisfy the password rules, and a new card number SHALL contain exactly 16 digits. Invalid values SHALL leave the user unchanged.

#### Scenario: Invalid new email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es` is executed
- **THEN** the update is rejected

#### Scenario: Email taken by another user
- **WHEN** `pedro@correo.com` belongs to another user and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is executed
- **THEN** the update is rejected

#### Scenario: Invalid new password or card
- **WHEN** `USER UPDATE` sets `PASSWORD` to `clave1!` or `CARD` to `1234`
- **THEN** the update is rejected

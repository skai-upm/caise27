# user-management Specification

## Purpose

Defines the users of the amusement park application: the data each user holds, the invariants on that data, and how users are stored and retrieved separately from the user representation and the application entry point.

## Requirements

### Requirement: User data
Each user SHALL hold the following data: first name, surname(s), DNI, email address, password, telephone number and card number. The data SHALL only be accessible through the user's operations (encapsulated attributes).

#### Scenario: User keeps its data
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** the user returns exactly those values for each attribute

### Requirement: DNI is immutable
A user's DNI SHALL NOT be changeable after the user is created.

#### Scenario: No DNI modification
- **WHEN** a user has been created with DNI `12345678Z`
- **THEN** the user offers no operation to change the DNI and keeps returning `12345678Z`

### Requirement: Separate user storage
Users SHALL be stored and retrieved by a component separate from the user representation and from the application entry point; the user itself SHALL NOT manage storage.

#### Scenario: Store and retrieve a user
- **WHEN** a user with email `ana@correo.com` is stored
- **THEN** it can be retrieved by that email address

#### Scenario: Unknown user
- **WHEN** a user is looked up by an email address that was never stored
- **THEN** no user is returned

### Requirement: DNI validation
A DNI SHALL be valid only when it consists of exactly eight digits followed by one letter, and that letter corresponds to the number according to the standard Spanish DNI algorithm (letter at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Valid DNI
- **WHEN** the DNI `12345678Z` is validated
- **THEN** it is accepted

#### Scenario: Wrong control letter
- **WHEN** the DNI `12345678A` is validated
- **THEN** it is rejected

#### Scenario: Wrong shape
- **WHEN** the DNIs `1234567Z`, `123456789Z` or `12345678` are validated
- **THEN** each is rejected

### Requirement: Email validation
An email address SHALL be valid only when it has the form `text@text.xxx`: non-empty content before a single `@`, and after it a non-empty domain followed by `.` and an extension of exactly three characters. The check SHALL use String operations and `split`, not regular expressions.

#### Scenario: Valid email
- **WHEN** the email `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** the emails `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` are validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only when it contains at least one uppercase letter, one lowercase letter, one digit and one symbol, checked without regular expressions or external libraries.

#### Scenario: Valid password
- **WHEN** the password `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Invalid passwords
- **WHEN** the passwords `clave1!` (no uppercase), `CLAVE1!` (no lowercase), `Clave!!` (no digit) or `Clave12` (no symbol) are validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only when it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Valid card
- **WHEN** the card number `1234567890123456` is validated
- **THEN** it is accepted

#### Scenario: Invalid cards
- **WHEN** the card numbers `1234 5678 9012 3456`, `123456789012345` or `12345678901234567` are validated
- **THEN** each is rejected

### Requirement: Telephone stored without validation
In Delivery 1 the telephone number SHALL only be stored as given, without validation, encryption or any other transformation.

#### Scenario: Telephone kept as given
- **WHEN** user data with telephone `612345678` is validated and stored
- **THEN** the telephone does not affect validity and is stored as `612345678`

### Requirement: Reusable user data validation
The system SHALL be able to validate a complete set of user data (DNI, email, password, card number) in one check that reports which fields are invalid, so that user operations can reuse it.

#### Scenario: All fields valid
- **WHEN** the data `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is validated
- **THEN** no errors are reported

#### Scenario: Several invalid fields
- **WHEN** user data with DNI `12345678A` and card `123` is validated
- **THEN** errors are reported for the DNI and the card number

### Requirement: User creation command
The application SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when all checks pass, store a new user with that data and confirm it with `USER CREATED`. The command SHALL have exactly seven data elements after the command name.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is printed and a user with email `ana@correo.com` and DNI `12345678Z` exists

#### Scenario: Wrong number of elements
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error describing the expected syntax is printed and no user is created

### Requirement: Validation before storing
User data SHALL be validated with the DNI, email, password and card rules before a user is stored; if any rule fails, no user SHALL be created and the failing fields SHALL be reported.

#### Scenario: Invalid DNI rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the DNI is printed and no user is created

#### Scenario: Invalid password rejected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456` is processed
- **THEN** an error about the password is printed and no user is created

### Requirement: Unique DNI and email
Two users SHALL NOT have the same DNI or the same email address; both conditions SHALL be checked before creating a user.

#### Scenario: Duplicate DNI rejected
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and email `otra@correo.com`
- **THEN** an error about the duplicate DNI is printed and no user is created

#### Scenario: Duplicate email rejected
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with DNI `87654321X` and email `ana@correo.com`
- **THEN** an error about the duplicate email is printed and no user is created

### Requirement: User deletion command
The application SHALL accept `USER DELETE;Email;Password` with exactly two data elements. It SHALL locate the user by email, verify the user exists, check that the password matches, delete the user and update the persisted users, confirming with `USER DELETED`.

#### Scenario: Successful deletion
- **WHEN** the user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed, the user no longer exists and is no longer in the users file

#### Scenario: Unknown user
- **WHEN** no user has email `nadie@correo.com` and `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error indicating the user does not exist is printed and no user is deleted

#### Scenario: Wrong number of elements
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** an error describing the expected syntax is printed and no user is deleted

### Requirement: Password required to delete
An incorrect password SHALL NOT allow a user to be deleted.

#### Scenario: Incorrect password
- **WHEN** the user `ana@correo.com` with password `Clave1!` exists and `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error about the incorrect password is printed and the user still exists

### Requirement: User update command
The application SHALL accept `USER UPDATE;Email;Password;Field;NewValue` with exactly four data elements and, when all checks pass, change the given field of the user identified by `Email` to `NewValue` and confirm with `USER UPDATED`.

#### Scenario: Update phone
- **WHEN** the user `ana@correo.com` with password `Clave1!` exists and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** `USER UPDATED` is printed and the user's phone is `699999999`

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed for that user
- **THEN** `USER UPDATED` is printed and the user's first name is `Ana Maria`

#### Scenario: Wrong number of elements
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE` is processed
- **THEN** an error describing the expected syntax is printed and nothing changes

### Requirement: Modifiable fields enumeration
The modifiable fields SHALL be represented by an enumeration containing exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. Any other field, including `DNI`, SHALL be rejected and the DNI SHALL remain unchanged.

#### Scenario: DNI cannot be modified
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is printed and the user's DNI is still `12345678Z`

#### Scenario: Unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error listing the modifiable fields is printed and nothing changes

### Requirement: Identification before update
Before any modification the current email and password SHALL be checked; an unknown email or incorrect password SHALL reject the update.

#### Scenario: Incorrect password
- **WHEN** `USER UPDATE;ana@correo.com;Otra1!;PHONE;699999999` is processed for a user whose password is `Clave1!`
- **THEN** an error is printed and the phone is unchanged

#### Scenario: Unknown user
- **WHEN** `USER UPDATE;nadie@correo.com;Clave1!;PHONE;699999999` is processed and no such user exists
- **THEN** an error is printed

### Requirement: Updated values keep creation restrictions
A new value SHALL satisfy the same validation rules as user creation: a new email SHALL be valid and not used by another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits. A rejected value SHALL leave the user unchanged.

#### Scenario: Email change
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** `USER UPDATED` is printed and the user is identified afterwards by `ana.nueva@correo.com` and not by `ana@correo.com`

#### Scenario: Email used by another user
- **WHEN** `pedro@correo.com` belongs to another user and `USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com` is processed
- **THEN** an error about the duplicate email is printed and Ana keeps `ana@correo.com`

#### Scenario: Invalid new values
- **WHEN** an update sets EMAIL to `ana@correo.es`, PASSWORD to `clave1!` or CARD to `123456789012345`
- **THEN** each update is rejected with an error and the user is unchanged

#### Scenario: Valid new password and card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` and then `USER UPDATE;ana@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** both print `USER UPDATED` and the user has password `Nueva2#` and card `9999888877776666`

### Requirement: Updates persist
A successful modification SHALL update the persisted users so that it is visible in later executions.

#### Scenario: Update survives restart
- **WHEN** a user's phone is updated to `699999999` and the application is started again
- **THEN** the user's phone is `699999999`

### Requirement: User deletion blocked by active tickets
`USER DELETE` SHALL be rejected while any active ticket (dated on or after the current system date) is associated with the user. Historical tickets alone SHALL NOT prevent deletion, and they SHALL keep the user information they reference after the user is deleted. This locked state SHALL be derived from the existing tickets and the current system date, not stored.

#### Scenario: Active ticket blocks deletion
- **WHEN** Ana has ticket `T001` dated `2027-04-15`, the system date is `2027-04-15` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists

#### Scenario: Future ticket deleted first
- **WHEN** the system date is `2027-04-01`, Ana's only ticket `T001` is dated `2027-04-15`, `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** both succeed

#### Scenario: Historical ticket does not block
- **WHEN** Ana's only ticket is dated `2027-04-15`, the system date is `2027-04-20` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and the ticket remains with Ana's data

### Requirement: User deletion blocked by locked groups
`USER DELETE` SHALL be rejected when the user has an active individual ticket that belongs to a group ticket, or belongs to a group that has an active group ticket, because deleting the user would modify a locked group.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to `G01`, `G01` has group ticket `TG01` dated `2027-04-15`, the system date is `2027-04-15` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists and still belongs to `G01`

#### Scenario: Only historical group tickets
- **WHEN** Ana belongs to `G01`, whose only group ticket is dated `2027-04-15`, the system date is `2027-04-16` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted, removed from `G01`, and `TG01` keeps Ana's composed ticket

### Requirement: Passwords protected with BCrypt
User passwords SHALL NOT be kept in plain text. After passing the Delivery 1 password rules, a password SHALL be stored as a BCrypt value generated with the added library; when a password is changed, a new BCrypt value SHALL be generated.

#### Scenario: Password stored protected
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the stored password of Ana is a BCrypt value different from `Clave1!` that BCrypt verifies against `Clave1!`

#### Scenario: Invalid password rejected before protection
- **WHEN** a user is created with password `clave1!`
- **THEN** the creation is rejected by the password rules and nothing is stored

#### Scenario: Password change generates a new value
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#` is processed
- **THEN** the stored value changes, BCrypt verifies it against `Nueva2#` and no longer against `Clave1!`

### Requirement: Password verification with BCrypt
Operations that require the user's password (USER DELETE, USER UPDATE) SHALL verify the entered password against the stored BCrypt value using the added library.

#### Scenario: Correct password accepted
- **WHEN** Ana's stored value protects `Clave1!` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the password is accepted and Ana is deleted

#### Scenario: Wrong password rejected
- **WHEN** `USER UPDATE;ana@correo.com;Otra1!;PHONE;699999999` is processed
- **THEN** an incorrect-password error is printed and nothing changes

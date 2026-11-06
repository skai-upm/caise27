# users Specification

## Purpose
Defines the user of the Amusement Park application as an independent, encapsulated domain object with an immutable DNI, and the separate component that stores and retrieves users.

## Requirements

### Requirement: User attributes
A user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. These attributes SHALL be accessible only through the user's operations, not as public fields.

#### Scenario: Create a user
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, a password, phone `612345678` and card `1234567890123456`
- **THEN** each value can be read back from the user

### Requirement: DNI is immutable
A user's DNI SHALL be fixed at creation and SHALL NOT be changeable afterwards. The other attributes MAY be changed through the user's operations.

#### Scenario: Attempt to change the DNI
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** the user's other attributes are changed
- **THEN** the DNI is still `12345678Z`, and the user offers no operation that changes it

### Requirement: Passwords are not kept in plain text by the user
The password attribute of a user SHALL hold the stored, protected value of the password, not the original text.

#### Scenario: User created with a protected password
- **GIVEN** the password `Clave1!` protected with BCrypt
- **WHEN** a user is created with that protected value
- **THEN** the user's password attribute does not contain `Clave1!`

### Requirement: Separate store for users
A component separate from the user class and from the application's main class SHALL store and retrieve users by email and by DNI. The user class SHALL NOT manage persistence. Central park management SHALL determine user existence and DNI through this component.

#### Scenario: Retrieve stored users
- **GIVEN** user `ana@correo.com` with DNI `12345678Z` is stored
- **WHEN** users are looked up by email `ana@correo.com` and by DNI `12345678Z`
- **THEN** the same user is returned both times

#### Scenario: Park management sees stored users
- **GIVEN** user `ana@correo.com` is stored and the application is initialized
- **WHEN** `TICKET CREATE;T001;ana@correo.com;<today>` is processed
- **THEN** the ticket is created for that user

### Requirement: DNI validation
A DNI SHALL be valid only if it consists of exactly eight digits followed by one uppercase letter equal to the letter at position (number mod 23) of `TRWAGMYFPDXBNJZSQVHLCKE`.

#### Scenario: Correct and incorrect check letters
- **WHEN** `12345678Z` and `12345678A` are validated
- **THEN** `12345678Z` is accepted and `12345678A` is rejected

#### Scenario: Wrong shape
- **WHEN** `1234567Z`, `123456789Z`, `1234567AZ` or `12345678` is validated
- **THEN** it is rejected

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: exactly one `@`, non-empty content before it, and after it a non-empty domain followed by a final `.` and an extension of exactly three characters, with no spaces. The check SHALL use String operations, not regular expressions.

#### Scenario: Valid email
- **WHEN** `ana@correo.com` is validated
- **THEN** it is accepted

#### Scenario: Invalid emails
- **WHEN** `ana`, `ana@correo`, `@correo.com`, `ana@.com` or `ana@correo.es` is validated
- **THEN** each is rejected

### Requirement: Password validation
A password SHALL be valid only if it contains at least one uppercase letter, one lowercase letter, one digit and one symbol (a character that is neither a letter, a digit nor whitespace). The check SHALL use neither regular expressions nor external libraries.

#### Scenario: Valid password
- **WHEN** `Clave1!` is validated
- **THEN** it is accepted

#### Scenario: Missing categories
- **WHEN** `clave1!`, `CLAVE1!`, `Clave!!` or `Clave12` is validated
- **THEN** each is rejected

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 digits, with no spaces, letters or symbols.

#### Scenario: Card numbers
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are validated
- **THEN** only `1234567890123456` is accepted

### Requirement: Whole-user validation for user operations
The system SHALL provide one validation of all the data of a user (first name, surnames, DNI, email, password, phone and card) that later user operations reuse. It SHALL reject the data with an invalid-input error naming the first invalid field. First name, surnames and phone SHALL only be required to be non-empty, and the phone SHALL be stored as given.

#### Scenario: Invalid field is named
- **WHEN** user data with DNI `12345678A` and otherwise valid values is validated
- **THEN** it is rejected with an error that mentions the DNI

### Requirement: Create a user
The system SHALL, for `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, validate all the data with the user data rules, check that no existing user has the same DNI or the same email, store the user with the password protected (never in plain text), and display `USER CREATED`. If any check fails or the command does not have exactly seven fields, it SHALL display an error message, add no user, and continue processing commands.

#### Scenario: Valid new user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is displayed
- **AND** user `ana@correo.com` with DNI `12345678Z` exists and its stored password is not `Clave1!` but verifies against it

#### Scenario: Duplicate DNI or email
- **GIVEN** user `ana@correo.com` with DNI `12345678Z` exists
- **WHEN** a USER CREATE uses DNI `12345678Z` with another email, or email `ana@correo.com` with another DNI
- **THEN** an error message is displayed and no user is added

#### Scenario: Malformed data
- **WHEN** USER CREATE is processed with an invalid DNI, email, password or card, or with a missing field
- **THEN** an error message is displayed and no user is added

### Requirement: DNI and email are unique
No two users SHALL share a DNI or an email address. Both conditions SHALL be checked before a user is stored.

#### Scenario: Both checked before storing
- **GIVEN** user `ana@correo.com` (DNI `12345678Z`) and user `pedro@correo.com` (DNI `87654321X`) exist
- **WHEN** a user with DNI `87654321X` and email `ana@correo.com` is created
- **THEN** it is rejected and the two existing users are unchanged

### Requirement: Users persist between executions
The system SHALL store the users in a text file and load them when the application starts, before processing any command. Every successful user operation SHALL be stored before the next command runs. Reading and writing the file SHALL be done by a component separate from the user class and from the main class, without databases, persistence frameworks or external persistence libraries.

#### Scenario: Users recovered after restart
- **GIVEN** an execution that created users `ana@correo.com` and `pedro@correo.com` and then ended
- **WHEN** the application starts again with the same data directory
- **THEN** both users exist with all their data, and creating `ana@correo.com` again is rejected as a duplicate

### Requirement: User file format
Each stored user SHALL occupy one line with its attributes separated by `;` in the order first name, surnames, DNI, email, password, phone, card. The password field SHALL contain the protected value, never the original password. A line that cannot be interpreted SHALL be reported and skipped.

#### Scenario: Line mapping
- **GIVEN** the stored line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<protected>;612345678;1234567890123456`
- **WHEN** the application loads it
- **THEN** a user exists with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, phone `612345678` and card `1234567890123456`, whose password verifies against the original one

#### Scenario: No plain-text password in the file
- **WHEN** a user is created with password `Clave1!`
- **THEN** the users file does not contain `Clave1!`

#### Scenario: Malformed line
- **GIVEN** the users file contains a line with fewer than seven fields
- **WHEN** the application starts
- **THEN** a warning is shown and the other users are loaded

### Requirement: Temporary mode isolates users
In `--fly` mode, users SHALL be stored only in the temporary directory, through the same repository as in normal mode, and SHALL disappear with it at the end of the execution.

#### Scenario: Fly run does not touch normal users
- **GIVEN** normal data with user `ana@correo.com`
- **WHEN** a `--fly` run creates `pedro@correo.com`
- **THEN** during that run `ana@correo.com` does not exist, and afterwards the normal users file still contains only `ana@correo.com`

### Requirement: Delete a user
The system SHALL, for `USER DELETE;Email;Password`, locate the user by email, verify that the user exists, check the password against the stored protected value, apply the user-deletion rules of the park (no active ticket, no membership of a locked group, removal from groups when allowed), delete the user, update the stored data, and display `USER DELETED`. If the user does not exist, the password is incorrect, a deletion rule prevents it, or the command does not have exactly two fields, it SHALL display an error message and change nothing.

#### Scenario: Correct email and password
- **GIVEN** user `ana@correo.com` with password `Clave1!` and no active tickets
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is displayed, the user no longer exists, and after a restart the user is still absent

#### Scenario: Incorrect password
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER DELETE;ana@correo.com;Otra1!` is processed
- **THEN** an error message is displayed and the user remains unchanged

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message is displayed

#### Scenario: Active ticket prevents deletion
- **GIVEN** user `ana@correo.com` has a ticket dated today
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message is displayed and the user and the ticket remain

#### Scenario: Deleted user leaves groups, historical tickets kept
- **GIVEN** user `ana@correo.com` belongs to modifiable group `G01` and only has tickets dated before today
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted, is no longer a member of `G01`, and the historical tickets still exist

### Requirement: Modifiable user fields
The fields that can be modified SHALL be represented by an enumeration with exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. The DNI SHALL NOT be modifiable.

#### Scenario: DNI or unknown field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` or `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error message is displayed and the user is unchanged

### Requirement: Update a user
The system SHALL, for `USER UPDATE;Email;Password;Field;NewValue`, check that a user with that email exists and that the password matches, validate the new value with the same rules as user creation, apply the change, store it, and display `USER UPDATED`. A new email SHALL be valid and not used by another user. A new password SHALL satisfy the password rules and be stored protected. A new card SHALL have exactly 16 digits. Names and phone SHALL be non-empty. Any failure SHALL display an error message and change nothing.

#### Scenario: Valid updates
- **GIVEN** user `ana@correo.com` with password `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria`, `…;CARD;9999888877776666` and `…;PASSWORD;Nueva2#` are processed
- **THEN** each displays `USER UPDATED`, and afterwards the user authenticates with `Nueva2#`, not `Clave1!`

#### Scenario: Wrong password
- **WHEN** `USER UPDATE;ana@correo.com;Mala1!;NAME;X` is processed
- **THEN** an error message is displayed and the user is unchanged

#### Scenario: Invalid new values
- **WHEN** an update sets EMAIL to `ana@correo.es` or to the email of another user, PASSWORD to `clave1!`, or CARD to `1234`
- **THEN** an error message is displayed and the user is unchanged

#### Scenario: Changes persist
- **GIVEN** `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` succeeded
- **WHEN** the application restarts
- **THEN** the user's phone is `699999999`

### Requirement: Email change keeps the user's park records
When a user's email changes, every park record that refers to that user SHALL refer to the new email: group memberships, individual tickets (including tickets in group compositions) and attraction occupancy. The user's passes and invoices therefore remain theirs. The change SHALL be stored.

#### Scenario: Tickets and groups follow the new email
- **GIVEN** `ana@correo.com` owns ticket `T001` and belongs to group `G01`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed
- **THEN** `T001` belongs to `ana.nueva@correo.com`, `G01` lists `ana.nueva@correo.com`, and `INVOICE CREATE;USER;ana.nueva@correo.com` lists `T001`
- **AND** the old email no longer identifies any user

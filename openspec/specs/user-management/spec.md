# user-management Specification

## Purpose

Defines the park user and the operations that create, delete and modify users while keeping user data valid and DNI and email unique.

## Requirements

### Requirement: User data
Each user SHALL hold a first name, surname(s), DNI, email address, password, telephone number and card number. The DNI SHALL NOT change after the user is created.

#### Scenario: DNI immutable
- **WHEN** any operation attempts to change an existing user's DNI
- **THEN** the DNI remains unchanged

### Requirement: User creation
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create a user only when all eight fields are present and non-empty and the DNI, email, password and card number pass validation. On success the application SHALL print `USER CREATED`.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is executed with no existing users
- **THEN** the user is stored and `USER CREATED` is printed

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is executed with an invalid DNI, email, password or card number
- **THEN** no user is created and an error message is printed

#### Scenario: Wrong number of fields
- **WHEN** `USER CREATE` is executed with fewer or more than seven data fields
- **THEN** no user is created and an error message is printed

### Requirement: User uniqueness
No two users SHALL share the same DNI or the same email address. Both conditions SHALL be checked before a user is created.

#### Scenario: Duplicate DNI
- **WHEN** a `USER CREATE` uses a DNI already registered
- **THEN** no user is created and an error message is printed

#### Scenario: Duplicate email
- **WHEN** a `USER CREATE` uses an email already registered
- **THEN** no user is created and an error message is printed

### Requirement: User deletion
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that the user exists, check the password, delete the user and update persistent data. On success the application SHALL print `USER DELETED`. Deletion SHALL be rejected while the user has any active ticket (an individual ticket or a group-composition ticket dated on or after the current system date), or while the user belongs to a group locked by an active group ticket. When deletion is allowed, the user SHALL also be removed from every group they belong to. Historical tickets of the user SHALL be retained.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is executed for an existing user with that password
- **THEN** the user is removed, the persisted file no longer contains it, and `USER DELETED` is printed

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is executed with an existing email and an incorrect password
- **THEN** the user is not deleted and an error message is printed

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is executed with an email that is not registered
- **THEN** nothing is deleted and an error message is printed

#### Scenario: Active individual ticket blocks deletion
- **WHEN** the user has a ticket dated today or later and `USER DELETE` is executed with correct credentials
- **THEN** an error is printed and the user still exists

#### Scenario: Active group-composition ticket blocks deletion
- **WHEN** the user is in the composition of a group ticket dated today or later and `USER DELETE` is executed with correct credentials
- **THEN** an error is printed and the user still exists

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group that has an active group ticket and `USER DELETE` is executed with correct credentials
- **THEN** an error is printed and the user still exists

#### Scenario: Historical tickets only
- **WHEN** the user has only historical tickets and belongs only to unlocked groups, and `USER DELETE` is executed with correct credentials
- **THEN** the user is deleted, removed from those groups, and their historical tickets remain

### Requirement: User modification
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL modify one field of the user identified by email after checking the current email and password. The field SHALL be one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. On success the application SHALL print `USER UPDATED` and persist the change.

#### Scenario: Successful update
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is executed for an existing user with that password
- **THEN** the user's first name becomes `Ana Maria`, the change is persisted, and `USER UPDATED` is printed

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is executed with an unknown email or an incorrect password
- **THEN** no field is changed and an error message is printed

#### Scenario: DNI not modifiable
- **WHEN** `USER UPDATE` is executed with field `DNI` or any field outside the allowed set
- **THEN** no field is changed and an error message is printed

### Requirement: Modification keeps data valid
A modification SHALL NOT produce a state that user creation would reject: a new email SHALL be valid and not used by another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits.

#### Scenario: Invalid new value
- **WHEN** `USER UPDATE` sets `EMAIL` to `ana@correo.es`, `PASSWORD` to `clave1!`, or `CARD` to `123456789012345`
- **THEN** no field is changed and an error message is printed

#### Scenario: Email taken by another user
- **WHEN** `USER UPDATE` sets `EMAIL` to an email registered to a different user
- **THEN** no field is changed and an error message is printed

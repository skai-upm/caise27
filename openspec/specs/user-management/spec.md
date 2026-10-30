# user-management Specification

## Purpose

Defines park users and the operations to create, delete and update them, including uniqueness of DNI and email and immutability of the DNI.

## Requirements

### Requirement: User attributes
Each user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. A user's DNI SHALL NOT change after creation.

#### Scenario: DNI cannot be modified
- **WHEN** a user exists with DNI `12345678Z` and any update operation is applied to that user
- **THEN** the user's DNI is still `12345678Z`

### Requirement: User creation
The system SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and create the user only if all fields are present and the DNI, email, password and card are valid, printing `USER CREATED` on success.

#### Scenario: Successful creation
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** `USER CREATED` is printed and the user exists

#### Scenario: Invalid data rejected
- **WHEN** a USER CREATE command has an invalid DNI, email, password or card
- **THEN** an `ERROR:` message is printed and no user is created

#### Scenario: Wrong number of arguments
- **WHEN** a USER CREATE command does not have exactly seven values or has a blank value
- **THEN** an `ERROR:` message is printed and no user is created

### Requirement: DNI and email uniqueness
The system SHALL reject creation of a user whose DNI or email address is already used by another user. Both conditions SHALL be checked before creating the user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and USER CREATE is processed with DNI `12345678Z` and a different email
- **THEN** an `ERROR:` message is printed and no user is created

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and USER CREATE is processed with email `ana@correo.com` and a different DNI
- **THEN** an `ERROR:` message is printed and no user is created

### Requirement: User deletion
The system SHALL accept `USER DELETE;Email;Password`, locate the user by email, verify it exists, check the password, delete the user and update persistent data, printing `USER DELETED` on success. Deletion SHALL be rejected while the user holds any active ticket (individual or composing a group ticket) or belongs to a locked group; otherwise the user is also removed from all their groups. Historical tickets SHALL be kept.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, also after restart

#### Scenario: Unknown email
- **WHEN** USER DELETE is processed with an email that no user has
- **THEN** an `ERROR:` message is printed and no user is deleted

#### Scenario: Incorrect password
- **WHEN** USER DELETE is processed with an existing email and a wrong password
- **THEN** an `ERROR:` message is printed and the user is not deleted

#### Scenario: Active ticket blocks deletion
- **WHEN** the user has an individual ticket, or a ticket composing a group ticket, dated on or after the current system date
- **THEN** an `ERROR:` message is printed and the user is not deleted

#### Scenario: Locked group blocks deletion
- **WHEN** the user belongs to a group that has an active group ticket not including the user
- **THEN** an `ERROR:` message is printed and the user is not deleted

#### Scenario: Deletion removes group membership and keeps history
- **WHEN** the user only has historical tickets and belongs only to unlocked groups
- **THEN** the user is deleted, removed from those groups, and the historical tickets still exist referring to the former user

### Requirement: User modification
The system SHALL accept `USER UPDATE;Email;Password;Field;NewValue` where Field is one of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. It SHALL check the current email and password first, apply the same validation as creation to the new value, and persist the change, printing `USER UPDATED`.

#### Scenario: Successful updates
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed for an existing user with those credentials
- **THEN** `USER UPDATED` is printed and the user's first name is `Ana Maria`

#### Scenario: Wrong credentials
- **WHEN** USER UPDATE is processed with an unknown email or a wrong password
- **THEN** an `ERROR:` message is printed and nothing changes

#### Scenario: DNI or unknown field
- **WHEN** USER UPDATE is processed with Field `DNI` or any value outside the allowed list
- **THEN** an `ERROR:` message is printed and nothing changes

#### Scenario: Invalid new value
- **WHEN** USER UPDATE sets EMAIL to an invalid address, PASSWORD to an invalid password, or CARD to a value that is not exactly 16 digits
- **THEN** an `ERROR:` message is printed and nothing changes

#### Scenario: Email already used by another user
- **WHEN** USER UPDATE sets EMAIL to an address used by a different user
- **THEN** an `ERROR:` message is printed and nothing changes

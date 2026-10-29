# user-management Specification

## Purpose

Defines the user operations available through commands — creation, deletion and modification — and the uniqueness and validation guarantees they preserve.

## Requirements

### Requirement: Create user
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create and store a new user when all data is valid and unique, and SHALL report `USER CREATED`.

#### Scenario: Successful creation
- **WHEN** the command `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed on an empty system
- **THEN** the output is `USER CREATED` and a user with email `ana@correo.com` exists

#### Scenario: Invalid data is rejected
- **WHEN** a `USER CREATE` command has an invalid DNI, email, password or card number
- **THEN** an error is reported and no user is created

#### Scenario: Wrong number of fields
- **WHEN** a `USER CREATE` command does not have exactly seven fields after the command name
- **THEN** an error is reported and no user is created

### Requirement: Unique DNI and email
No two users SHALL have the same DNI or the same email address; both conditions SHALL be checked before a user is created.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and a different email
- **THEN** an error is reported and no user is created

#### Scenario: Duplicate email
- **WHEN** a user with email `ana@correo.com` exists and `USER CREATE` is processed with that email and a different valid DNI
- **THEN** an error is reported and no user is created

### Requirement: Delete user
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify it exists, check the password, verify the user has no active ticket (standalone or inside a group ticket) and belongs to no locked group, then remove the user from all groups (and any attraction), delete the user, update persistent data and report `USER DELETED`. Historical tickets SHALL be retained.

#### Scenario: Successful deletion
- **WHEN** user `pedro@correo.com` with password `Otra2#` exists and `USER DELETE;pedro@correo.com;Otra2#` is processed
- **THEN** the output is `USER DELETED`, the user no longer exists and it is absent from the persistence file

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed for an email that no user has
- **THEN** an error is reported and no user is deleted

#### Scenario: Wrong password
- **WHEN** `USER DELETE` is processed with an existing email and an incorrect password
- **THEN** an error is reported and the user is not deleted

#### Scenario: Active individual ticket blocks deletion
- **WHEN** Ana has an individual ticket dated on or after the system date and `USER DELETE` is processed for Ana
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Active group ticket blocks deletion
- **WHEN** Ana's only ticket is inside an active group ticket and `USER DELETE` is processed for Ana
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Member of a locked group
- **WHEN** Ana has no active ticket herself but belongs to a group locked by an active group ticket
- **THEN** `USER DELETE` for Ana is rejected

#### Scenario: Historical tickets do not block and are kept
- **WHEN** Ana has only historical tickets and belongs to an unlocked group
- **THEN** `USER DELETE` for Ana succeeds, she is removed from the group and her historical tickets still exist

### Requirement: Modifiable fields enum
The fields modifiable by `USER UPDATE` SHALL be represented by an enumeration with exactly the values `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`. The DNI SHALL NOT be modifiable.

#### Scenario: DNI is not an updatable field
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is reported and the user's DNI is unchanged

#### Scenario: Unknown field
- **WHEN** `USER UPDATE` is processed with a field that is not one of the six enum values
- **THEN** an error is reported and the user is unchanged

### Requirement: Update user
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL verify the current email and password, validate the new value with the creation rules, apply it, update persistent data and report `USER UPDATED`.

#### Scenario: Successful updates
- **WHEN** user `ana@correo.com` / `Clave1!` exists and `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed
- **THEN** the output is `USER UPDATED` and the user's first name is `Ana Maria`, also after restarting the application

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with an unknown email or an incorrect password
- **THEN** an error is reported and no user is modified

#### Scenario: Invalid new value
- **WHEN** `USER UPDATE` sets `EMAIL` to an invalid email, `PASSWORD` to an invalid password, or `CARD` to a value that is not 16 digits
- **THEN** an error is reported and the user is unchanged

#### Scenario: New email already used by another user
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and Ana's `EMAIL` is updated to `pedro@correo.com`
- **THEN** an error is reported and Ana's email is unchanged

#### Scenario: Email change takes effect
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` succeeds
- **THEN** the user is found by `ana.nueva@correo.com` and no longer by `ana@correo.com`

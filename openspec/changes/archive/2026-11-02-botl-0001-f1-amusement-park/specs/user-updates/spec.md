# Spec Delta

## Purpose

Defines the `USER UPDATE` command that modifies authorised user fields after authentication, while keeping DNI immutable and all creation-time rules in force (F1-C06).

## ADDED Requirements

### Requirement: USER UPDATE command
The system SHALL accept `USER UPDATE;Email;Password;Field;NewValue`. Before any modification it SHALL check that a user with the current email exists and that the password matches. On success it SHALL apply the change, update the persistent information, and report success (for example `USER UPDATED`).

#### Scenario: Update email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed with correct credentials and the new email is unused
- **THEN** the user's email becomes `ana.nueva@correo.com` and is persisted

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with an unknown email or a wrong password
- **THEN** no user is modified and an error is reported

### Requirement: Modifiable fields enumeration
The modifiable fields SHALL be represented by an enumeration with exactly the values `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`. Any other field name, including `DNI`, SHALL be rejected without modifying the user.

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** the user's first name becomes `Ana Maria` and is persisted

#### Scenario: DNI not modifiable
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the user is unchanged and an error is reported

### Requirement: Updated values meet creation rules
A new value SHALL satisfy the same validation rules as user creation: a new email SHALL be valid and not used by another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits. A rejected update SHALL leave the user and the users file unchanged.

#### Scenario: Email already used by another user
- **WHEN** a user updates EMAIL to the email of another existing user
- **THEN** the user is unchanged and an error is reported

#### Scenario: Invalid card
- **WHEN** a user updates CARD to `1234`
- **THEN** the user is unchanged and an error is reported

#### Scenario: Invalid password
- **WHEN** a user updates PASSWORD to `clave1!`
- **THEN** the user is unchanged and an error is reported

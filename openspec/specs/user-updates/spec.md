# user-updates Specification

## Purpose

Modifies permitted user fields with the `USER UPDATE` command after authentication, keeping DNI immutable and every creation-time validation and uniqueness rule in force.

## Requirements

### Requirement: USER UPDATE command
The system SHALL accept `USER UPDATE;Email;Password;Field;NewValue`, verify the current email and password before any modification, apply the change, update persistent storage and report `USER UPDATED`.

#### Scenario: Update name
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed with correct credentials
- **THEN** Ana's first name becomes `Ana Maria` in memory and in storage, and `USER UPDATED` is reported

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE` is processed with an unknown email or an incorrect password
- **THEN** no user is modified and an error is reported

### Requirement: Enum-defined modifiable fields
The modifiable fields SHALL be represented by an enumeration with exactly the values `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`; any other field name, including `DNI`, SHALL be rejected without modifying the user.

#### Scenario: DNI cannot be updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the user is unchanged and an error is reported

### Requirement: Updates keep creation rules
A new value SHALL satisfy the same rules as at creation: a new email SHALL be valid and not used by another user, a new password SHALL meet the password rules, and a new card number SHALL contain exactly 16 digits. A rejected value SHALL leave the user unchanged.

#### Scenario: Valid available email
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and the new email is unused
- **THEN** the new email is stored and persisted, and the user is afterwards identified by `ana.nueva@correo.com`

#### Scenario: Email used by another user
- **WHEN** EMAIL is updated to an address already registered by another user
- **THEN** the user is unchanged and an error is reported

#### Scenario: Invalid password or card
- **WHEN** PASSWORD is updated to `clave1!` or CARD is updated to `1234 5678 9012 3456`
- **THEN** the user is unchanged and an error is reported

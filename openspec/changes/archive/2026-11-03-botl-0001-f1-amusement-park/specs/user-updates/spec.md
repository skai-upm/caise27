# Spec Delta

## Purpose

Allows authenticated modification of a user's permitted fields through `USER UPDATE`, without ever producing a state that creation would reject (source F1-C06, section 9).

## ADDED Requirements

### Requirement: Update user command
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL, after verifying that a user with the current email exists and the password matches, set the named field to the new value, update the persistent information, and report success (for example `USER UPDATED`).

#### Scenario: Name updated
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria` is processed for an existing user with those credentials
- **THEN** the user's first name becomes `Ana Maria`, the change is persisted and success is reported

#### Scenario: Email updated and persisted
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com` is processed and no other user has that email
- **THEN** the user's email becomes `ana.nueva@correo.com` and it is still the email after a restart

### Requirement: Modifiable fields are enum-defined
The modifiable fields SHALL be exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`, represented by an enumeration; any other field name, including `DNI`, SHALL be rejected without modifying the user.

#### Scenario: DNI update rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error is reported and the user's DNI is unchanged

#### Scenario: Unknown field rejected
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;AGE;30` is processed
- **THEN** an error is reported and the user is unchanged

### Requirement: Update requires valid credentials
`USER UPDATE` SHALL NOT modify any user when no user has the given email or the password is incorrect.

#### Scenario: Wrong password
- **WHEN** `USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999` is processed for a user whose password is `Clave1!`
- **THEN** an error is reported and the user's telephone is unchanged

### Requirement: Updated values follow creation rules
A new EMAIL SHALL be valid and not used by another user, a new PASSWORD SHALL meet the password rules and a new CARD SHALL contain exactly 16 digits; otherwise the update SHALL be rejected and the user left unchanged.

#### Scenario: Email already used by another user
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and Ana's EMAIL is updated to `pedro@correo.com`
- **THEN** an error is reported and Ana's email is unchanged

#### Scenario: Invalid card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;CARD;1234` is processed
- **THEN** an error is reported and the card number is unchanged

#### Scenario: Invalid password
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave` is processed
- **THEN** an error is reported and the password is unchanged

## ADDED Requirements

### Requirement: Modifiable user fields
The fields a user may modify SHALL be represented by an enumeration with exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`; the DNI SHALL NOT be modifiable.

#### Scenario: DNI not modifiable
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error message is displayed and the user's DNI is unchanged

### Requirement: USER UPDATE command
The system SHALL accept `USER UPDATE;Email;Password;Field;NewValue` and, after verifying that the user with that e-mail exists and the password matches, change the given field to the new value when it satisfies the creation rules: a new e-mail must be valid and not used by another user, a new password must satisfy the password rules (and is stored as a new protected value), a new card must contain exactly 16 digits. Otherwise an error message SHALL be displayed and nothing SHALL change. Modifications SHALL be persisted.

#### Scenario: Update name, e-mail, password and card
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria`, `USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com`, `USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#` and `USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666` are processed
- **THEN** each displays `USER UPDATED` and, also after restarting, the user is found by `ana.nueva@correo.com`, named `Ana Maria`, authenticates with `Nueva2#` and has card `9999888877776666`

#### Scenario: Wrong credentials
- **WHEN** `USER UPDATE;ana@correo.com;Clave1?;PHONE;699999999` or `USER UPDATE;nadie@correo.com;Clave1!;PHONE;699999999` is processed
- **THEN** an error message is displayed and no user changes

#### Scenario: Invalid or duplicate new values
- **WHEN** the new e-mail is `ana@correo.es` or belongs to another user, the new password is `clave1!`, or the new card is `1234`
- **THEN** an error message is displayed and the field keeps its previous value

#### Scenario: References follow an e-mail change
- **WHEN** a group member changes their e-mail
- **THEN** the group still contains that user, now identified by the new e-mail

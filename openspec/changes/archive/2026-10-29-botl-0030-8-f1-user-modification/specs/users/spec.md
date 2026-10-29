# Spec Delta

## ADDED Requirements

### Requirement: Modifiable user fields
The fields that can be modified SHALL be represented by an enumeration containing exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`; the DNI SHALL NOT be modifiable, and any other field name SHALL be rejected with an error message.

#### Scenario: DNI not modifiable
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** an error message is shown and the DNI is unchanged

### Requirement: USER UPDATE command
The command `USER UPDATE;Email;Password;Field;NewValue` SHALL check the current email and password before any modification and then change the field, applying the same rules as user creation: a new email SHALL be valid and not used by another user, a new password SHALL satisfy the password rules and be stored in a newly protected form, and a new card SHALL have exactly 16 digits. Rejected updates SHALL change nothing; accepted updates SHALL be persisted.

#### Scenario: Source examples
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria`, then `...;PASSWORD;Nueva2#`, then `USER UPDATE;ana@correo.com;Nueva2#;CARD;9999888877776666`, then `USER UPDATE;ana@correo.com;Nueva2#;EMAIL;ana.nueva@correo.com` are processed
- **THEN** each shows `USER UPDATED`, and the user is found by `ana.nueva@correo.com` with name `Ana Maria`, card `9999888877776666` and password `Nueva2#`

#### Scenario: Wrong current password
- **WHEN** `USER UPDATE;ana@correo.com;clave1!;PHONE;699999999` is processed
- **THEN** an error message is shown and the phone is unchanged

#### Scenario: Invalid or duplicate new values
- **WHEN** the new email is `ana@correo.es` or the email of another user, the new password is `clave1!`, or the new card is `123`
- **THEN** an error message is shown and the user is unchanged

#### Scenario: Changes persist
- **WHEN** the phone of Ana is changed and the application restarts
- **THEN** the new phone is loaded

#### Scenario: Email change keeps the user's purchases
- **WHEN** Ana has ticket `T001` and changes her email
- **THEN** `T001` still belongs to Ana and her invoice is available under the new email

# Spec Delta

## ADDED Requirements

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

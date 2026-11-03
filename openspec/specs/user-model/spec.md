# user-model Specification

## Purpose
Defines the data held for each park user, its encapsulation, the immutability of the DNI and the closed set of fields users may edit.

## Requirements

### Requirement: User data
The system SHALL hold, for each user, a first name, surname(s), DNI, email address, password, telephone number and card number, accessible only through the user's own operations (encapsulated state).

#### Scenario: All seven fields retained
- **GIVEN** the command `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeded
- **WHEN** the stored user is inspected
- **THEN** it holds first name `Ana`, surname(s) `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`

### Requirement: Immutable DNI
The DNI SHALL be fixed when the user is created and SHALL NOT be modifiable afterwards by any operation.

#### Scenario: No way to change the DNI
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the command is rejected as an unknown field and the user's DNI remains `12345678Z`

### Requirement: Editable fields enumeration
The editable fields SHALL be exactly `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` and `CARD`, represented as an enumeration and named this way in `USER UPDATE` commands.

#### Scenario: Each editable field can be updated
- **GIVEN** an existing user and correct credentials
- **WHEN** a `USER UPDATE` names any of `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` with a valid new value
- **THEN** that field takes the new value

#### Scenario: Unknown field name
- **WHEN** a `USER UPDATE` names a field that is not one of the six editable fields
- **THEN** the command is rejected and no data changes

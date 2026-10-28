## ADDED Requirements

### Requirement: User attributes persisted
The user file SHALL store, for each user, DNI, e-mail, protected password, first name, surname(s), telephone number and card number, and restore them on the next execution; lines written by earlier versions with fewer fields SHALL still load.

#### Scenario: Full user after restart
- **WHEN** a user with all attributes is saved and the application restarts
- **THEN** the user is restored with the same first name, surnames, DNI, e-mail, protected password, telephone and card

#### Scenario: Earlier user line
- **WHEN** the user file contains `12345678Z;ana@correo.com`
- **THEN** the user loads with that DNI and e-mail and empty remaining attributes

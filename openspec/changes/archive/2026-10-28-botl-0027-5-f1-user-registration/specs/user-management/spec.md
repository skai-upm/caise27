## ADDED Requirements

### Requirement: USER CREATE command
The system SHALL accept `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` and, when all the data satisfies the user validation rules and no active user has the same DNI or e-mail, register the user with all its attributes (the password only in protected form) and display a confirmation. Otherwise, or with a wrong number of fields or empty fields, an error message SHALL be displayed, no user SHALL be created and processing SHALL continue.

#### Scenario: Create a user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is displayed and user `ana@correo.com` exists with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, telephone `612345678`, card `1234567890123456` and a protected password that verifies `Clave1!`

#### Scenario: Invalid data rejected
- **WHEN** `USER CREATE` is processed with DNI `12345678A`, or e-mail `ana@correo.es`, or password `clave1!`, or card `1234 5678 9012 3456`
- **THEN** an error message is displayed and no user is created

#### Scenario: Malformed command
- **WHEN** `USER CREATE;Ana;Garcia Lopez` is processed
- **THEN** an error message is displayed and the next command is still processed

### Requirement: User uniqueness
Two users SHALL NOT have the same DNI or the same e-mail address; both conditions SHALL be checked before creating a user.

#### Scenario: Duplicate DNI
- **WHEN** a user with DNI `12345678Z` exists and `USER CREATE` is processed with DNI `12345678Z` and a new e-mail
- **THEN** an error message is displayed and no user is created

#### Scenario: Duplicate e-mail
- **WHEN** a user with e-mail `ana@correo.com` exists and `USER CREATE` is processed with that e-mail and a new valid DNI
- **THEN** an error message is displayed and no user is created

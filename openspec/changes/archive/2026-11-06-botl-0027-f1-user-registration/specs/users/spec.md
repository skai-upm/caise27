# Spec Delta

## ADDED Requirements

### Requirement: Create a user
The system SHALL, for `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card`, validate all the data with the user data rules, check that no existing user has the same DNI or the same email, store the user with the password protected (never in plain text), and display `USER CREATED`. If any check fails or the command does not have exactly seven fields, it SHALL display an error message, add no user, and continue processing commands.

#### Scenario: Valid new user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is displayed
- **AND** user `ana@correo.com` with DNI `12345678Z` exists and its stored password is not `Clave1!` but verifies against it

#### Scenario: Duplicate DNI or email
- **GIVEN** user `ana@correo.com` with DNI `12345678Z` exists
- **WHEN** a USER CREATE uses DNI `12345678Z` with another email, or email `ana@correo.com` with another DNI
- **THEN** an error message is displayed and no user is added

#### Scenario: Malformed data
- **WHEN** USER CREATE is processed with an invalid DNI, email, password or card, or with a missing field
- **THEN** an error message is displayed and no user is added

### Requirement: DNI and email are unique
No two users SHALL share a DNI or an email address. Both conditions SHALL be checked before a user is stored.

#### Scenario: Both checked before storing
- **GIVEN** user `ana@correo.com` (DNI `12345678Z`) and user `pedro@correo.com` (DNI `87654321X`) exist
- **WHEN** a user with DNI `87654321X` and email `ana@correo.com` is created
- **THEN** it is rejected and the two existing users are unchanged

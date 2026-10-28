## ADDED Requirements

### Requirement: USER DELETE command
The system SHALL accept `USER DELETE;Email;Password` and, in this order, locate the user by e-mail, verify that it exists, check the password against the stored protected value, delete the user and update the persistent data. An unknown e-mail or an incorrect password SHALL be rejected with an error message without deleting anything; the ticket and group restrictions on user deletion SHALL still apply.

#### Scenario: Delete with the correct password
- **WHEN** user `ana@correo.com` with password `Clave1!` and no active tickets exists and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is displayed and the user no longer exists, also after restarting the application

#### Scenario: Incorrect password
- **WHEN** `USER DELETE;ana@correo.com;Clave1?` is processed
- **THEN** an error message is displayed and the user still exists

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message is displayed

#### Scenario: Active ticket still blocks deletion
- **WHEN** `ana@correo.com` has a ticket dated today and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message is displayed and the user still exists

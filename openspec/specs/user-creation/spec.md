# user-creation Specification

## Purpose

Lets the application register new users from the `USER CREATE` command while enforcing data validation and uniqueness of DNI and email.

## Requirements

### Requirement: USER CREATE command
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL register a new user when all fields are present and non-blank and DNI, email, password and card pass validation; on success the application reports `USER CREATED`. Commands SHALL be interpreted with basic String operations and `split` on `;`. (F1-C03)

#### Scenario: Unique valid user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` runs on an empty system
- **THEN** exactly one user is registered and `USER CREATED` is reported

#### Scenario: Invalid data
- **WHEN** `USER CREATE` runs with an invalid DNI, email, password or card number
- **THEN** no user is registered, an error is reported and processing continues with the next command

#### Scenario: Malformed command
- **WHEN** `USER CREATE` runs with fewer or more than seven data fields, or with a blank field
- **THEN** no user is registered, an error is reported and processing continues

### Requirement: DNI and email uniqueness
Before creating a user the application SHALL check that no existing user has the same DNI and that no existing user has the same email address; if either check fails, the user SHALL NOT be created. (F1-C03)

#### Scenario: Duplicate DNI
- **WHEN** `USER CREATE` runs with a DNI already registered and a new email
- **THEN** no additional user is registered and an error is reported

#### Scenario: Duplicate email
- **WHEN** `USER CREATE` runs with an email already registered and a new DNI
- **THEN** no additional user is registered and an error is reported

### Requirement: Robust command interpretation
An incorrect or unknown command SHALL NOT cause the application to terminate unexpectedly; it SHALL be reported as an error and processing SHALL continue. (F1-C03)

#### Scenario: Unknown command
- **WHEN** `FOO BAR;1;2` is processed
- **THEN** an error is reported, state is unchanged and the next command is processed

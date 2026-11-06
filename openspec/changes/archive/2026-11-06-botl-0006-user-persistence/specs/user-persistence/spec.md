# Spec Delta

## Purpose

Preserves the Amusement Park users between runs of the application using a plain text file with one `;`-separated line per user.

## ADDED Requirements

### Requirement: User file format
Users SHALL be stored in a text file with one user per line and the attributes separated by `;` in the order first name, surname(s), DNI, email, password, telephone, card number, exactly as they were supplied.

#### Scenario: Created user is written as one line
- **GIVEN** an empty user file
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** the file contains exactly the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

#### Scenario: Rejected command does not change the file
- **GIVEN** a user file containing user `12345678Z`
- **WHEN** a `USER CREATE` command with a duplicate DNI is rejected
- **THEN** the file content is unchanged

### Requirement: Users loaded at start-up
When the application starts it SHALL load every stored user from the user file, mapping each attribute to its field by position; if the file does not exist the application MUST start with no users and without error.

#### Scenario: Users recovered after restart
- **GIVEN** users were created in a previous run
- **WHEN** the application is started again
- **THEN** those users exist with the same attributes, and creating another user with one of their DNIs or emails is rejected as a duplicate

#### Scenario: Persisted line mapped to fields
- **GIVEN** the user file line `Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654`
- **WHEN** the users are loaded
- **THEN** the user with DNI `87654321X` has first name `Pedro`, surnames `Lopez Martin`, email `pedro@correo.com`, password `Otra2#`, telephone `623456789` and card `9876543210987654`

#### Scenario: Missing file
- **GIVEN** no user file exists
- **WHEN** the application starts
- **THEN** it starts normally with no users

#### Scenario: Malformed line ignored
- **GIVEN** a user file containing a valid line and a line without exactly seven `;`-separated fields
- **WHEN** the users are loaded
- **THEN** the valid user is loaded, the malformed line is skipped, and the application still starts

### Requirement: Persistence kept apart from user and entry point
Reading and writing the user file SHALL be implemented in a component separate from the user representation and the application entry point, without databases, persistence frameworks or external libraries.

#### Scenario: Storage component is independent
- **WHEN** the source code is inspected
- **THEN** file reading and writing for users is performed only by the dedicated persistence component, using the Java standard library

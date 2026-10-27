# Spec Delta

## Purpose

Defines how the application's state is preserved between runs using plain text files, and how it is restored when the application starts again.

## ADDED Requirements

### Requirement: Users file format
Users SHALL be stored in a text file where each user occupies one line with its attributes separated by `;` in the order first name, surnames, DNI, email, password, telephone, card number.

#### Scenario: Stored line format
- **WHEN** the user Ana with surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456` is stored
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Users preserved between runs
Users created in one execution SHALL be available in later executions: previously stored users SHALL be loaded when the application starts.

#### Scenario: User survives a restart
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed, the application ends, and the application starts again
- **THEN** the user with email `ana@correo.com` exists in the new execution

#### Scenario: Loaded users keep uniqueness
- **WHEN** the users file already contains a user with DNI `12345678Z` and the application processes a `USER CREATE` with DNI `12345678Z`
- **THEN** the creation is rejected as a duplicate DNI

#### Scenario: No users file yet
- **WHEN** the application starts and the users file does not exist
- **THEN** it starts with no users and without error

### Requirement: Persistence isolated from model and entry point
Reading and writing the users file SHALL be done by a component separate from the user representation and the application entry point, using only the Java standard library (no databases, persistence frameworks or external persistence libraries).

#### Scenario: Responsibilities separated
- **WHEN** the source code is inspected
- **THEN** neither the user class nor the main class performs file reading or writing of users

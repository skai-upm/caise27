# Spec Delta

## Purpose

Defines which data the system keeps for each user and which of it can never change after the user is created.

## ADDED Requirements

### Requirement: User data
The system SHALL store, for each user, first name, surname(s), DNI, email address, password, telephone number and card number, and SHALL expose them only through the user's own operations (encapsulated state).

#### Scenario: All attributes are retained
- **GIVEN** a valid `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** it is processed
- **THEN** the stored user holds exactly those seven values

### Requirement: Immutable DNI
The DNI of a user SHALL NOT change after the user has been created; no command SHALL offer a way to modify it.

#### Scenario: DNI is not an editable field
- **GIVEN** an existing user `ana@correo.com` / `Clave1!`
- **WHEN** `USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X` is processed
- **THEN** the command is rejected and the user's DNI remains `12345678Z`

# Spec Delta

## Purpose

Keeps tickets, including historical ones, across application executions in a plain text file.

## ADDED Requirements

### Requirement: Ticket file format
The system SHALL store tickets in a text file, one per line, as `Id;DNI;yyyy-MM-dd;Precio`, where `DNI` identifies the owning user and the price has two decimals.

#### Scenario: Line written for a ticket
- **WHEN** Ana (`12345678Z`) buys `T001` for 2026-10-15 at 40.00
- **THEN** the ticket file contains `T001;12345678Z;2026-10-15;40.00`

### Requirement: Save after ticket changes
After every successful TICKET CREATE or TICKET DELETE the ticket file SHALL reflect the current tickets; rejected commands SHALL leave it unchanged.

#### Scenario: Rejected purchase leaves file intact
- **WHEN** a TICKET CREATE for a nonexistent user is rejected
- **THEN** the ticket file content is unchanged

### Requirement: Restore tickets at startup
On startup the system SHALL load all stored tickets after the users, linking each ticket to the loaded user with the stored DNI. A ticket whose user no longer exists SHALL be kept as a historical record identified by the stored DNI. A missing file SHALL mean no tickets.

#### Scenario: Restart restores tickets
- **GIVEN** a previous run created `T001` for Ana
- **WHEN** the application restarts with the same files and `SYSTEM DATE;2026-10-01` is processed
- **THEN** `USER DELETE;ana@correo.com;Clave1!` is still rejected and `TICKET CREATE;T001;...` is rejected as a duplicate

#### Scenario: Historical ticket of a deleted user
- **GIVEN** a stored ticket whose DNI matches no registered user
- **WHEN** the application starts
- **THEN** the ticket is loaded and saved again unchanged

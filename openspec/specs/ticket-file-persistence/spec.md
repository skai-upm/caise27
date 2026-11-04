# ticket-file-persistence Specification

## Purpose

Defines how tickets are stored in a plain text file, restored on startup linked to their users, and retained as history.

## Requirements

### Requirement: Ticket records
The system SHALL store each ticket on one line of a text file as `Id;yyyy-MM-dd;Precio;DNI`, referencing the user by immutable DNI, and SHALL rewrite the file after every successful ticket creation or deletion; rejected commands SHALL leave it unchanged.

#### Scenario: File content
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds at price `40.00`
- **THEN** the tickets file contains `T001;2026-10-15;40.00;12345678Z`

### Requirement: Restart restoration
On startup the system SHALL load tickets after users, linking each ticket to the registered user with its DNI; a missing file SHALL mean no tickets.

#### Scenario: Tickets restored
- **GIVEN** tickets `T001` and `T002` were stored
- **WHEN** the application restarts
- **THEN** both tickets exist with their dates and prices and refer to the registered user records

### Requirement: History of deleted users retained
A stored ticket whose DNI matches no registered user (its user was deleted) SHALL be kept unchanged in the file as a historical record, and its identifier SHALL remain reserved.

#### Scenario: Ticket of deleted user survives
- **GIVEN** Ana has only past ticket `T001` and is deleted
- **WHEN** the application restarts and later saves tickets
- **THEN** the line for `T001` is still in the file and `TICKET CREATE;T001;...` is rejected as a duplicate identifier

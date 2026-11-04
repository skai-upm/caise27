# Spec Delta

## Purpose

Defines how individual tickets are stored in a text file, restored at startup with their owners, and retained as historical records.

## ADDED Requirements

### Requirement: Ticket file format
The system SHALL store tickets in a text file (`tickets.txt`, next to the user file) with one ticket per line in the form `Identificador;DNI;yyyy-MM-dd;Precio`, identifying the owner by DNI and writing the price with two decimals.

#### Scenario: Stored line
- **WHEN** ticket `T001` for Ana (`12345678Z`) dated 2026-10-15 at 40.00 has been created
- **THEN** the ticket file contains the line `T001;12345678Z;2026-10-15;40.00`

### Requirement: Ticket file updated after every successful change
After every successful `TICKET CREATE` or `TICKET DELETE` the ticket file SHALL reflect exactly the current tickets; a rejected command SHALL leave it unchanged.

#### Scenario: Deleted ticket disappears from the file
- **WHEN** a future ticket is deleted
- **THEN** its line no longer appears in the ticket file

### Requirement: Tickets restored at startup and retained as history
On startup the system SHALL load all tickets after the users, resolving each DNI to the registered user object; tickets SHALL never be removed because their date has passed, and a ticket whose DNI matches no registered user SHALL be retained as history with that DNI.

#### Scenario: Restart restores tickets
- **GIVEN** tickets `T001` (2026-10-15, 40.00) and `T002` (2026-11-01, 60.00) for Ana were saved and the application stopped
- **WHEN** the application starts again
- **THEN** both tickets exist with the same dates and prices and their owner is the registered user Ana

#### Scenario: Restored future ticket still blocks deletion
- **GIVEN** a restored ticket of Ana dated on or after the system date
- **WHEN** `USER DELETE` is processed for Ana with correct credentials
- **THEN** the deletion is rejected

#### Scenario: History of a deleted user is retained
- **GIVEN** Ana had only past tickets and was deleted
- **WHEN** the application restarts
- **THEN** her tickets are still in the ticket file and loaded, identified by DNI `12345678Z`

#### Scenario: First start without a ticket file
- **WHEN** no ticket file exists at startup
- **THEN** the application starts with no tickets and does not fail

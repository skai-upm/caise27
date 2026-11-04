# Spec Delta

## Purpose

Defines how tickets are stored in a text file and restored, linked to the existing users, when the application starts again.

## ADDED Requirements

### Requirement: Ticket file format
Tickets SHALL be stored in a UTF-8 text file separate from the users and groups files, one ticket per line, as `Id;yyyy-MM-dd;DNI;Precio` with the owner's DNI and the price formatted with two decimals. The current ticket price setting SHALL NOT be stored.

#### Scenario: File content
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds at price 40.00 for the user with DNI `12345678Z`
- **THEN** the tickets file contains the line `T001;2026-10-15;12345678Z;40.00`

### Requirement: Ticket file reflects every successful change
After each successful `TICKET CREATE` or `TICKET DELETE` the tickets file SHALL contain exactly the current tickets. A rejected command SHALL NOT change it.

#### Scenario: Rejected ticket command
- **WHEN** a `TICKET` command is rejected
- **THEN** the tickets file content is unchanged

### Requirement: Ticket restart recovery
On startup the application SHALL load all tickets, past and future, after the users, and link each ticket to the loaded user with the stored DNI without creating new user objects. A ticket whose DNI matches no user SHALL be kept as a historical record without a user reference. A missing tickets file SHALL mean no tickets.

#### Scenario: Restore tickets after restart
- **GIVEN** tickets `T001` (2026-10-15, 40.00) and `T002` (2026-10-20, 60.00) for Ana were created in a previous run
- **WHEN** the application restarts with the same files
- **THEN** both tickets exist with the same dates and prices, and refer to the same user object returned by user lookup

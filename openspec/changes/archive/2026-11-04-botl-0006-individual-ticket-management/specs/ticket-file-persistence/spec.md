# Spec Delta

## Purpose

Defines how tickets are stored in a plain text file and restored, linked to their owners, when the application starts.

## ADDED Requirements

### Requirement: Ticket file format
The system SHALL store tickets in a text file, one ticket per line, as `id;ownerDni;yyyy-MM-dd;price` with the price written with two decimals.

#### Scenario: File contents
- **WHEN** ticket `T001` for Ana (`12345678Z`) dated 2026-10-15 at 40.00 is saved
- **THEN** the file contains `T001;12345678Z;2026-10-15;40.00`

### Requirement: Persist after successful ticket changes
After each successful `TICKET CREATE` or `TICKET DELETE` the tickets file SHALL contain all current tickets; rejected commands SHALL NOT change it.

#### Scenario: Rejected creation leaves file untouched
- **GIVEN** a saved ticket `T001`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-20` is rejected as a duplicate
- **THEN** the tickets file content is unchanged

### Requirement: Restore tickets at startup
At startup the system SHALL load the tickets file after the users, linking each ticket to the loaded user with the stored DNI (the same object, not a copy). Tickets whose owner no longer exists SHALL be kept as history with the stored owner DNI. A missing file SHALL mean no tickets; malformed lines SHALL be ignored.

#### Scenario: Restart restores tickets
- **GIVEN** a previous run created `T001` for Ana dated 2026-10-15 at 40.00
- **WHEN** the application restarts
- **THEN** `T001` exists with the same date and price, and its owner is the loaded Ana user object

#### Scenario: Restored ticket still blocks deletion
- **GIVEN** a previous run created `T001` for Ana dated 2026-10-15
- **WHEN** the application restarts, `SYSTEM DATE;2026-10-01` is processed and Ana's deletion is attempted
- **THEN** deletion is rejected

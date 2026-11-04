# individual-tickets Specification

## Purpose
Lets registered users buy individual tickets for a date of use, records the price paid, and keeps past tickets as history, using the application's configured system date for every check.

## Requirements

### Requirement: Ticket data
Each ticket SHALL have an identifier, a date of use, a reference to its owning registered user, and the price captured when it was created.

#### Scenario: Ticket fields
- **GIVEN** system date 2026-10-01, price 40.00 and registered user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds
- **THEN** ticket `T001` has date 2026-10-15, price 40.00, and its owner is the same user object as the registered Ana

### Requirement: Ticket creation
`TICKET CREATE;Id;Correo;yyyy-MM-dd` SHALL create a ticket for the registered user with that email (case-insensitive), at the current ticket price, and reply exactly `OK TICKET CREATED -> <price>` with the price shown with two decimals. The identifier SHALL be nonblank and unique, and the date SHALL be a valid `yyyy-MM-dd` date on or after the current system date.

#### Scenario: Successful purchase
- **GIVEN** system date 2026-10-01 and registered user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the reply is `OK TICKET CREATED -> 40.00` and the ticket is stored

#### Scenario: Ticket for today
- **GIVEN** system date 2026-10-01
- **WHEN** a ticket is created for 2026-10-01
- **THEN** creation succeeds

#### Scenario: Nonexistent user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is processed
- **THEN** an `ERROR` message is produced and no ticket is created

#### Scenario: Past date
- **GIVEN** system date 2026-10-01
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** an `ERROR` message is produced and no ticket is created

#### Scenario: Invalid input
- **WHEN** the identifier is blank or already used, the date is malformed (e.g. `15-10-2026`, `2026-02-30`), or the parameter count is wrong
- **THEN** an `ERROR` message is produced and no ticket is created

### Requirement: Ticket deletion
`TICKET DELETE;Id` SHALL delete the ticket only when its date is strictly later than the current system date; a ticket dated on or before the system date, or an unknown identifier, SHALL be rejected.

#### Scenario: Future ticket
- **GIVEN** system date 2026-10-01 and ticket `T001` dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an `OK` message is produced and the ticket no longer exists

#### Scenario: Ticket for today or the past
- **GIVEN** ticket `T001` dated 2026-10-15
- **WHEN** the system date is 2026-10-15 or 2026-10-16 and `TICKET DELETE;T001` is processed
- **THEN** an `ERROR` message is produced and the ticket is kept

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed
- **THEN** an `ERROR` message is produced

### Requirement: Historical tickets retained
Tickets whose date is earlier than the system date SHALL be kept as historical records, including after their owner has been deleted.

#### Scenario: History after user deletion
- **GIVEN** ticket `T001` dated 2026-10-15 for Ana and system date 2026-10-16
- **WHEN** Ana is deleted and the application restarts
- **THEN** ticket `T001` still exists with its date, price and Ana's DNI as owner identifier

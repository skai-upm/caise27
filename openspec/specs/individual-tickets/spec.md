# individual-tickets Specification

## Purpose
Lets registered users buy individual tickets for a date of use, cancel future tickets, and keeps the rules that depend on ticket dates evaluated against the configured system date.

## Requirements

### Requirement: Ticket data
Each ticket SHALL have an identifier, a date of use, a reference to the registered user who owns it (the same user object, not a copy) and the price captured when it was created.

#### Scenario: Ticket fields
- **GIVEN** the system date is 2026-10-01, the ticket price is 40.00 and user `ana@correo.com` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** ticket `T001` exists with date 2026-10-15, price 40.00 and owner the registered user Ana

### Requirement: Create ticket
The system SHALL process `TICKET CREATE;Identificador;Correo;yyyy-MM-dd`, creating a ticket at the current ticket price when the identifier is non-blank and not used by any ticket, a registered user has that email, and the date is a valid `yyyy-MM-dd` date not earlier than the system date; on success it SHALL answer exactly `OK TICKET CREATED -> <Precio>` with the price written with two decimals.

#### Scenario: Successful creation
- **GIVEN** the system date is 2026-10-01 and user `ana@correo.com` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the response is `OK TICKET CREATED -> 40.00`

#### Scenario: Ticket for today
- **GIVEN** the system date is 2026-10-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2026-10-15` is processed
- **THEN** the ticket is created

#### Scenario: Nonexistent user
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2026-10-15` is processed
- **THEN** an `ERROR:` response is printed and no ticket is created

#### Scenario: Past date
- **GIVEN** the system date is 2026-10-01
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** an `ERROR:` response is printed and no ticket is created

#### Scenario: Duplicate identifier or invalid date
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-20` or `TICKET CREATE;T004;ana@correo.com;2026-10-32` is processed
- **THEN** an `ERROR:` response is printed and no ticket is created

### Requirement: Delete ticket
The system SHALL process `TICKET DELETE;Identificador`, deleting the ticket only if it exists and its date is strictly later than the system date.

#### Scenario: Future ticket deleted
- **GIVEN** the system date is 2026-10-01 and ticket `T001` is dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an `OK:` response is printed and `T001` no longer exists

#### Scenario: Ticket for today or the past cannot be deleted
- **GIVEN** ticket `T001` is dated 2026-10-15
- **WHEN** the system date is 2026-10-15 or 2026-10-16 and `TICKET DELETE;T001` is processed
- **THEN** an `ERROR:` response is printed and `T001` is kept

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an `ERROR:` response is printed

### Requirement: Current and future tickets block user deletion
A user SHALL NOT be deleted while they own any ticket whose date is on or after the system date (`ticketDate >= SYSTEM DATE`); tickets dated before the system date SHALL NOT by themselves block the deletion.

#### Scenario: Future ticket blocks deletion
- **GIVEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15` were processed
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` response is printed and Ana is still registered

#### Scenario: Ticket for today blocks deletion
- **GIVEN** the system date equals the date of Ana's ticket
- **WHEN** Ana is deleted with correct credentials
- **THEN** the deletion is rejected

#### Scenario: Past ticket does not block deletion
- **GIVEN** Ana's only ticket `T001` is dated 2026-10-15 and `SYSTEM DATE;2026-10-16` was processed
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and ticket `T001` is retained as a historical record

### Requirement: Ticket commands in HELP
The `HELP` response SHALL include `TICKET CREATE;Identificador;Correo;yyyy-MM-dd`, `TICKET DELETE;Identificador` and `SYSTEM PRICE;Precio`.

#### Scenario: Help shows ticket commands
- **WHEN** `HELP` is processed
- **THEN** the response contains the three lines

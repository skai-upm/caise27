# tickets Specification

## Purpose
Lets registered users buy individual park tickets for a given date, keeps tickets as persistent history and applies date-dependent rules using the system date.

## Requirements

### Requirement: Buy an individual ticket
The system SHALL create a ticket with `TICKET CREATE;Id;Correo;yyyy-MM-dd` for the registered user with that email, priced at the current individual price, and respond `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals. The identifier SHALL be non-empty and unique, the user SHALL exist and the date SHALL be valid and not earlier than the system date.

#### Scenario: Ticket for an existing user
- **WHEN** `SYSTEM DATE;2026-10-01` and `TICKET CREATE;T001;ana@correo.com;2026-10-15` are processed and Ana is registered
- **THEN** the response is `OK TICKET CREATED -> 40.00` and ticket T001 holds Ana, 2026-10-15 and 40.00

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Duplicate identifier or past date
- **WHEN** `TICKET CREATE` uses an existing identifier, or a date earlier than the system date
- **THEN** an error is reported and no ticket is created

### Requirement: Ticket data
Every ticket SHALL have an identifier, a date of use, a reference to its holder user and the price paid.

#### Scenario: Holder reference
- **WHEN** ticket T001 is created for Ana and Ana's name is later updated
- **THEN** T001's holder shows the updated name

### Requirement: Current ticket price
The individual ticket price SHALL start at `40.00` on every execution, SHALL be changeable with `SYSTEM PRICE;<importe>` (a positive amount with at most two decimals) and SHALL NOT be persisted. A price change SHALL NOT alter tickets already bought.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` and then `TICKET CREATE;T003;ana@correo.com;2026-10-20` are processed
- **THEN** the response is `OK TICKET CREATED -> 60.00` and earlier tickets keep their price

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;-5` or `SYSTEM PRICE;abc` is processed
- **THEN** an error is reported and the price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the application restarts after `SYSTEM PRICE;60.00`
- **THEN** new tickets cost `40.00`

### Requirement: Delete a future ticket
The system SHALL delete a ticket with `TICKET DELETE;Id` only when its date is strictly later than the system date; otherwise it SHALL report an error and keep the ticket.

#### Scenario: Future ticket
- **WHEN** the system date is 2026-10-01 and `TICKET DELETE;T001` is processed for a ticket dated 2026-10-15
- **THEN** the ticket is deleted

#### Scenario: Ticket for today or the past
- **WHEN** the system date equals or is after the ticket date and `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket remains

### Requirement: Ticket persistence and history
Tickets SHALL be stored in a text file and restored on start-up with their identifier, date, price and holder. Tickets whose date has passed SHALL remain stored as history.

#### Scenario: Restart
- **WHEN** T001 is created and the application restarts
- **THEN** T001 exists with the same date, price and holder

#### Scenario: Past tickets kept
- **WHEN** the system date moves past T001's date
- **THEN** T001 is still stored

### Requirement: Ticket commands in HELP
`HELP` SHALL list `TICKET CREATE;Id;Correo;yyyy-MM-dd`, `TICKET DELETE;Id` and `SYSTEM PRICE;Importe`.

#### Scenario: Help shows ticket commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the three syntaxes

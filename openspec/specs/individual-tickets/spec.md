# individual-tickets Specification

## Purpose

Lets registered users buy dated individual park tickets at the current base price, keeps tickets and their history persistently, and governs refunds.

## Requirements

### Requirement: Ticket data
Each individual ticket SHALL hold an identifier unique among all tickets, a use date, a reference to its user, and the purchase price recorded when it was bought.

#### Scenario: Ticket fields
- **GIVEN** base price 40.00 and registered user `ana@correo.com`
- **WHEN** ticket `T001` is created for 2026-10-15
- **THEN** `T001` references that user, has date 2026-10-15 and price 40.00

### Requirement: Runtime base price
The base ticket price SHALL be 40.00 at the start of every execution and SHALL be changeable with `SYSTEM PRICE;Precio` to a nonnegative decimal amount; an invalid amount SHALL be rejected without change. The base price itself SHALL NOT be persisted, and changing it SHALL NOT alter tickets already bought.

#### Scenario: Change price
- **GIVEN** ticket `T001` bought at 40.00
- **WHEN** `SYSTEM PRICE;60.00` is executed and ticket `T002` is created
- **THEN** `T002` costs 60.00 and `T001` still costs 40.00

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc` or `SYSTEM PRICE;-5` is executed
- **THEN** the command is rejected and the base price is unchanged

#### Scenario: Price resets each execution
- **GIVEN** `SYSTEM PRICE;60.00` was executed in a previous run
- **WHEN** the application starts again and a ticket is created
- **THEN** the ticket costs 40.00

### Requirement: Create ticket command
`TICKET CREATE;IdTicket;Correo;yyyy-MM-dd` SHALL create a ticket for the registered user with that email, at the current base price, for a date on or after the system date, replying `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals. An identifier already used by any ticket, an unknown email, a malformed date or a date before the system date SHALL be rejected.

#### Scenario: Successful purchase
- **GIVEN** system date 2026-10-01, base price 40.00 and registered user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is executed
- **THEN** the reply is `OK TICKET CREATED -> 40.00` and the ticket is stored

#### Scenario: Invalid purchase
- **WHEN** `TICKET CREATE` names an unknown email, an existing ticket identifier, a malformed date, or a date before the system date
- **THEN** the command is rejected and no ticket is stored

### Requirement: Ticket persistence and history
Tickets SHALL be persisted after every change and restored at startup with their recorded price and user. Past tickets SHALL be retained as historical records, including tickets whose user has since been deleted.

#### Scenario: Tickets survive restart
- **GIVEN** ticket `T001` was bought at 60.00
- **WHEN** the application restarts
- **THEN** `T001` exists with price 60.00, its date and its user

### Requirement: Delete ticket command
`TICKET DELETE;IdTicket` SHALL delete an individual ticket only if its date is strictly later than the system date. Tickets dated today or earlier, unknown identifiers, and tickets that belong to a group ticket SHALL be rejected.

#### Scenario: Future-only ticket refund
- **GIVEN** system date 2026-10-10 and tickets dated 2026-10-09, 2026-10-10 and 2026-10-11
- **WHEN** each is submitted to `TICKET DELETE`
- **THEN** only the ticket dated 2026-10-11 is deleted

#### Scenario: Constituent ticket cannot be deleted individually
- **GIVEN** a future group ticket
- **WHEN** `TICKET DELETE` names one of its constituent tickets
- **THEN** the command is rejected

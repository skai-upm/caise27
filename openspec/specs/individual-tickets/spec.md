# individual-tickets Specification

## Purpose

Lets registered users buy dated individual tickets whose sale price is recorded, kept as history, and used to restrict deletions.

## Requirements

### Requirement: Create individual ticket
`TICKET CREATE;<ticketId>;<email>;<yyyy-MM-dd>` SHALL create a ticket for the registered user with that email, for that date, priced at the current base price, and SHALL respond `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals. The ticket identifier SHALL be unique among all tickets and group tickets, the user SHALL exist, and the date SHALL be valid and not earlier than the system date; otherwise the command SHALL be rejected without creating a ticket.

#### Scenario: Valid purchase
- **WHEN** the system date is 2026-10-01, the base price is 40.00, and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is executed for registered user Ana
- **THEN** the output is `OK TICKET CREATED -> 40.00` and ticket `T001` holds Ana, 2026-10-15 and price 40.00

#### Scenario: Unknown user or duplicate identifier
- **WHEN** the email is not registered or the identifier `T001` already exists
- **THEN** an error is shown and no ticket is created

#### Scenario: Past date
- **WHEN** the system date is 2026-10-01 and a ticket is requested for 2026-09-30
- **THEN** an error is shown and no ticket is created

### Requirement: Recorded price is immutable
A ticket SHALL keep the price it was sold at; later `SYSTEM PRICE` changes SHALL NOT modify existing tickets.

#### Scenario: Price change after purchase
- **WHEN** `T001` was bought at 40.00 and then `SYSTEM PRICE;60.00` is executed
- **THEN** `T001` still has price 40.00 and a new ticket is sold at 60.00

### Requirement: Delete individual ticket
`TICKET DELETE;<ticketId>` SHALL delete a standalone individual ticket only when its date is strictly later than the system date. A ticket dated today or earlier, an unknown identifier, or a ticket that is a constituent of a group ticket SHALL be rejected without changes.

#### Scenario: Future-only ticket refund
- **WHEN** the system date is 2026-10-15 and tickets dated 2026-10-14, 2026-10-15, and 2026-10-16 are each submitted to `TICKET DELETE`
- **THEN** only the 2026-10-16 ticket is deleted

#### Scenario: Constituent ticket
- **WHEN** `TICKET DELETE` names an individual ticket that belongs to a group ticket
- **THEN** an error is shown and the group ticket and all its constituents remain

### Requirement: Ticket persistence and history
Individual tickets SHALL be persisted after every successful change and restored at startup linked to their users. Past tickets SHALL be retained, including when their user is later deleted; such a ticket SHALL keep its holder's identifying data (DNI, email, name, surname) as recorded.

#### Scenario: Tickets survive restart
- **WHEN** `T001` was created and the application restarts
- **THEN** `T001` exists with the same date, price, and user

#### Scenario: History after user deletion
- **WHEN** Ana's only ticket is in the past, Ana is deleted, and the application restarts
- **THEN** the ticket still exists and identifies Ana by DNI, email, name, and surname

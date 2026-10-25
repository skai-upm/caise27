# ticketing Specification

## Purpose

Sells individual and group tickets for specific dates, prices them (with group discounts), governs their deletion, and keeps them as persistent historical records.

## Requirements

### Requirement: Create individual ticket
`TICKET CREATE;Id;Correo;Fecha` SHALL create a ticket with the given unique identifier, date of use (`yyyy-MM-dd`), the existing user with that email and the current individual price, replying `OK TICKET CREATED -> <Precio>` with two decimals. Unknown users, duplicate ticket identifiers, invalid dates and dates before the current date SHALL be rejected.

#### Scenario: Buy ticket
- **WHEN** the price is 40.00 and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is executed with date 2026-10-01
- **THEN** the reply is `OK TICKET CREATED -> 40.00` and T001 references Ana

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is executed
- **THEN** an error is reported and no ticket is created

#### Scenario: Duplicate identifier
- **WHEN** T001 exists and `TICKET CREATE;T001;ana@correo.com;2026-10-20` is executed
- **THEN** an error is reported

### Requirement: Delete individual ticket
`TICKET DELETE;Id` SHALL delete an individual ticket only if its date is strictly later than the current date, cascade-deleting all its SpeedPasses and its Special Pass. Tickets dated today or in the past, unknown tickets, and individual tickets that belong to a group ticket SHALL NOT be deleted, and a rejected deletion SHALL leave the ticket and its passes intact.

#### Scenario: Future ticket
- **WHEN** the date is 2026-10-01 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** the ticket is deleted

#### Scenario: Ticket for today
- **WHEN** the date is 2026-10-15 and `TICKET DELETE;T001` is executed for a ticket dated 2026-10-15
- **THEN** an error is reported and the ticket remains

#### Scenario: Passes cascade
- **WHEN** a future ticket with SpeedPasses and a Special Pass is deleted
- **THEN** its passes no longer exist, also after a restart

### Requirement: Create group ticket
`GROUPTICKET CREATE;Id;Grupo;Fecha` SHALL, for an existing non-empty group, create a group ticket containing one individual ticket per member at the time of purchase, each priced at the current individual price. The total SHALL be the sum of those prices minus a discount by member count: fewer than 3 → 0 %, 3–5 → 10 %, 6–10 → 20 %, more than 10 → 50 %. The reply SHALL be `OK GROUP TICKET CREATED -> <Precio>`.

#### Scenario: Discount brackets
- **WHEN** group tickets are bought at 40.00 for groups of 2, 3, 5, 6, 10 and 11 members
- **THEN** the totals are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00

#### Scenario: Composition frozen at purchase
- **WHEN** a group ticket is bought for G01 with 2 members and the group later gains a member
- **THEN** the group ticket still contains exactly 2 individual tickets

#### Scenario: Empty or unknown group
- **WHEN** `GROUPTICKET CREATE;TG01;G99;2026-10-15` is executed for a missing or empty group
- **THEN** an error is reported

### Requirement: Delete group ticket
`GROUPTICKET DELETE;Id` SHALL, only if its date is strictly later than the current date, delete first the passes of each of its internal individual tickets, then those internal tickets, and finally the group ticket. A rejected deletion SHALL leave everything intact.

#### Scenario: Future group ticket
- **WHEN** the date is 2026-10-01 and a group ticket dated 2026-10-15 is deleted
- **THEN** the group ticket, its internal tickets and their passes no longer exist

#### Scenario: Group ticket for today
- **WHEN** the date equals the group ticket date
- **THEN** deletion is rejected and the group ticket, internal tickets and passes remain

### Requirement: Ticket identifiers
Ticket identifiers SHALL be unique across individual tickets, group tickets and the internal tickets generated for group tickets.

#### Scenario: Same id for group ticket
- **WHEN** individual ticket T001 exists and `GROUPTICKET CREATE;T001;G01;2026-10-15` is executed
- **THEN** an error is reported

### Requirement: Ticket persistence and history
Individual tickets (with user, date, price and owning group ticket if any) and group tickets (with group, date and total price) SHALL be stored in text files and restored at startup. Past tickets SHALL be retained as historical records, even if their user or group is later deleted.

#### Scenario: Restart
- **WHEN** tickets are created, the application exits and starts again
- **THEN** the tickets exist with the same users, dates and prices

#### Scenario: Past tickets kept
- **WHEN** the current date moves beyond a ticket's date
- **THEN** the ticket still exists

# ticket-management Specification

## Purpose

Defines how individual tickets are bought and cancelled, the current ticket price, the data each ticket holds, its persistence as a historical record, and how ticket dates are evaluated against the system date.

## Requirements

### Requirement: Ticket data
Each individual ticket SHALL have a unique identifier, a date of use, a reference to the registered user it belongs to, and the price charged when it was bought.

#### Scenario: Ticket fields
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` succeeds while the price is 40.00
- **THEN** ticket `T001` has date 2026-10-15, refers to Ana's user object, and has price 40.00

### Requirement: Create individual ticket
The command `TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` SHALL create a ticket for the registered user with that email, charging the current ticket price, and print `OK TICKET CREATED -> <price>` with the price written with two decimals. It SHALL fail with an `ERROR` line, creating nothing, if no user has that email, the identifier is blank or already used, the date is not a valid `yyyy-MM-dd` date, or the date is earlier than the system date.

#### Scenario: Ticket bought
- **WHEN** user Ana exists, the system date is 2026-10-01 and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the ticket is created and `OK TICKET CREATED -> 40.00` is printed

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is processed and no user has that email
- **THEN** no ticket is created and an `ERROR` line is printed

#### Scenario: Duplicate identifier
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;ana@correo.com;2026-10-20` is processed
- **THEN** no ticket is created and an `ERROR` line is printed

#### Scenario: Date in the past
- **WHEN** the system date is 2026-10-01 and `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** no ticket is created and an `ERROR` line is printed

### Requirement: Ticket price
The current individual ticket price SHALL be an internal value initialised to 40.00 at the start of every execution and not persisted. The command `SYSTEM PRICE;<price>` SHALL set it to a positive amount with at most two decimals and print `OK SYSTEM PRICE SET -> <price>`; other values SHALL be rejected with an `ERROR` line, leaving the price unchanged. Tickets already bought keep the price they were charged.

#### Scenario: Change price
- **WHEN** `SYSTEM PRICE;60.00` is processed and then a ticket is bought
- **THEN** `OK SYSTEM PRICE SET -> 60.00` is printed and the ticket is created with `OK TICKET CREATED -> 60.00`

#### Scenario: Invalid price
- **WHEN** `SYSTEM PRICE;abc`, `SYSTEM PRICE;-5` or `SYSTEM PRICE;0` is processed
- **THEN** an `ERROR` line is printed and the price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price was changed to 60.00 and the application restarts
- **THEN** the current price is 40.00 and existing tickets still have the price they were charged

### Requirement: Delete individual ticket
The command `TICKET DELETE;<id>` SHALL delete the ticket and print `OK TICKET DELETED` only when the ticket date is strictly later than the system date; a ticket dated on or before the system date, or an unknown identifier, SHALL be rejected with an `ERROR` line.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2026-10-01 and ticket `T001` is dated 2026-10-15
- **THEN** `TICKET DELETE;T001` deletes it and prints `OK TICKET DELETED`

#### Scenario: Ticket for today
- **WHEN** the system date is 2026-10-15 and ticket `T001` is dated 2026-10-15
- **THEN** `TICKET DELETE;T001` is rejected with an `ERROR` line

#### Scenario: Past ticket
- **WHEN** the system date is 2026-10-16 and ticket `T001` is dated 2026-10-15
- **THEN** `TICKET DELETE;T001` is rejected with an `ERROR` line and the ticket is kept

### Requirement: Ticket persistence
Tickets SHALL be stored in a text file and restored when the application starts, linked to the loaded user objects, keeping identifier, date, user and the price charged. Past tickets SHALL be retained as historical records and SHALL NOT be removed when the system date moves past them.

#### Scenario: Tickets survive restart
- **WHEN** ticket `T001` is created, the application terminates and starts again
- **THEN** `T001` exists with the same date, user and price

#### Scenario: Past tickets retained
- **WHEN** the system date is moved after the date of ticket `T001`
- **THEN** `T001` still exists

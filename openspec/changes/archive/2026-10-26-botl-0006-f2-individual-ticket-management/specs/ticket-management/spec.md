# Spec Delta

## Purpose

Lets registered users buy individual tickets for a date of use, keeps every ticket (including past ones) persistently, and manages the current ticket price.

## ADDED Requirements

### Requirement: Ticket data
Each ticket SHALL have an identifier, a date of use, the registered user it belongs to (a reference to that user, not a copy) and the price paid.

#### Scenario: Ticket fields
- **WHEN** ticket `T001` is sold to `ana@correo.com` for 2026-10-15 at price 40.00
- **THEN** the ticket has identifier `T001`, date 2026-10-15, user Ana and price 40.00

### Requirement: Create individual ticket
The command `TICKET CREATE;Id;Correo;yyyy-MM-dd` SHALL create a ticket for the registered user with that email, for that date, at the current ticket price, and SHALL answer exactly `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals. The identifier SHALL be non-empty and not used by any existing ticket, the user SHALL exist, and the date SHALL be a valid `yyyy-MM-dd` date not earlier than the system date; otherwise an error SHALL be reported and no ticket created.

#### Scenario: Ticket created
- **WHEN** the system date is 2026-10-01, user `ana@correo.com` exists and `TICKET CREATE;T001;ana@correo.com;2026-10-15` is received
- **THEN** ticket `T001` is stored and `OK TICKET CREATED -> 40.00` is printed

#### Scenario: Nonexistent user
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2026-10-15` is received
- **THEN** an error is printed and no ticket is created

#### Scenario: Duplicate identifier
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;ana@correo.com;2026-10-20` is received
- **THEN** an error is printed and ticket `T001` is unchanged

#### Scenario: Invalid or past date
- **WHEN** the system date is 2026-10-01 and the date parameter is `2026-13-01` or `2026-09-30`
- **THEN** an error is printed and no ticket is created

### Requirement: Current ticket price
The application SHALL keep a current ticket price that starts at `40.00` on every execution and is not persisted. The command `SYSTEM PRICE;<importe>` SHALL change it to a positive decimal amount with at most two decimals; other values SHALL be rejected leaving the price unchanged. Already sold tickets SHALL keep the price they were sold at.

#### Scenario: Price changed
- **WHEN** `SYSTEM PRICE;60.00` is received and then `TICKET CREATE;T003;ana@correo.com;2026-10-20`
- **THEN** the response is `OK TICKET CREATED -> 60.00`

#### Scenario: Invalid price
- **WHEN** any of `SYSTEM PRICE;-5`, `SYSTEM PRICE;0`, `SYSTEM PRICE;abc`, `SYSTEM PRICE;10.555` is received
- **THEN** an invalid-price error is printed and the current price is unchanged

#### Scenario: Price resets on restart
- **WHEN** the price was changed to 60.00 and the application is restarted
- **THEN** the current price is 40.00 and tickets sold before keep their stored price

### Requirement: Delete ticket
The command `TICKET DELETE;Id` SHALL delete the ticket only if it exists and its date is strictly later than the system date; a ticket dated on or before the system date SHALL NOT be deleted and an error SHALL be reported.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2026-10-01 and ticket `T001` is dated 2026-10-15 and `TICKET DELETE;T001` is received
- **THEN** ticket `T001` is deleted

#### Scenario: Ticket for today
- **WHEN** the system date is 2026-10-15 and ticket `T001` is dated 2026-10-15 and `TICKET DELETE;T001` is received
- **THEN** an error is printed and the ticket is kept

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is received and no such ticket exists
- **THEN** an error is printed

### Requirement: Ticket persistence and history
All tickets, including those whose date is already past, SHALL be stored in a text file and restored when the application starts, linked to the registered user they belong to. Past tickets SHALL be retained as historical records, including when their user has been deleted.

#### Scenario: Restart restores tickets
- **WHEN** ticket `T001` is created, the application is restarted
- **THEN** ticket `T001` exists with its date, price and the restored user Ana

#### Scenario: History kept after user deletion
- **WHEN** the system date is 2026-10-16, Ana's only ticket `T001` is dated 2026-10-15 and Ana is deleted
- **THEN** ticket `T001` is still stored as a historical record

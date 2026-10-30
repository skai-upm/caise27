# individual-tickets Specification

## Purpose

Lets registered users buy dated individual tickets whose purchase price is fixed at purchase time, with refund rules based on the system date and retention of past tickets as history.

## Requirements

### Requirement: Individual ticket model
An individual ticket SHALL have an identifier, a date of use, a reference to its user, and its purchase price. Ticket identifiers SHALL be unique across individual tickets, group tickets and the tickets composing group tickets.

#### Scenario: Duplicate ticket identifier
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;pedro@correo.com;2026-10-15` is executed
- **THEN** an error is reported and no ticket is created

### Requirement: Create individual ticket
`TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` SHALL create a ticket for the registered user with that email, for a date on or after the system date, priced at the current base price, and SHALL print exactly `OK TICKET CREATED -> <Precio>` with the price formatted with two decimals and `.` as separator.

#### Scenario: Successful purchase
- **GIVEN** system date 2026-10-01, base price 40.00 and user `ana@correo.com`
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is executed
- **THEN** the output is `OK TICKET CREATED -> 40.00`

#### Scenario: Rejected purchases
- **WHEN** the email is not registered, the date is malformed, or the date is before the system date
- **THEN** an error is reported and no ticket is created

#### Scenario: Price fixed at purchase
- **GIVEN** ticket `T001` bought at 40.00
- **WHEN** `SYSTEM PRICE;60.00` is executed
- **THEN** `T001` keeps the price 40.00

### Requirement: Delete individual ticket
`TICKET DELETE;<id>` SHALL delete a standalone individual ticket only when its date is strictly after the system date. Tickets dated today or earlier, unknown identifiers, and tickets that belong to a group ticket SHALL be rejected.

#### Scenario: Only future tickets can be deleted
- **GIVEN** system date 2026-10-10 and tickets dated 2026-10-09, 2026-10-10 and 2026-10-11
- **WHEN** each is submitted to `TICKET DELETE`
- **THEN** only the ticket dated 2026-10-11 is deleted

### Requirement: Ticket persistence and history
Individual tickets SHALL be stored in a text file with their purchase price and restored at startup. Past tickets SHALL be kept as historical records, including after their user has been deleted, preserving the user's identifying data (name, surname, DNI, email).

#### Scenario: Tickets survive restart
- **GIVEN** ticket `T001` for Ana priced 40.00
- **WHEN** the application restarts
- **THEN** `T001` exists, references Ana and is priced 40.00

#### Scenario: Historical ticket of a deleted user
- **GIVEN** a past ticket for Ana and Ana has been deleted
- **WHEN** the application restarts
- **THEN** the ticket still exists with Ana's DNI and email

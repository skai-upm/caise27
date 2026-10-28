# tickets Specification

## Purpose
Defines tickets: the common abstraction shared by all ticket types (identifier, date of use, price), the shared identifier space, and individual tickets with their creation and deletion rules relative to the current system date.

## Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and a price, and all ticket types SHALL share this behavior through a common abstraction so operations that do not need the concrete type can handle any ticket. A ticket SHALL be active when its date is on or after the current system date and historical otherwise.

#### Scenario: Active and historical tickets
- **WHEN** the current system date is 2027-04-15
- **THEN** a ticket dated 2027-04-15 or later is active and a ticket dated 2027-04-14 or earlier is historical

### Requirement: Shared ticket identifier space
Ticket identifiers SHALL be unique across all ticket types and SHALL be checked in one common way for every ticket type, so a new ticket is rejected with an `ERROR:` message when any existing ticket already uses its identifier.

#### Scenario: Duplicate identifier
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;pedro@correo.com;2027-04-20` is processed
- **THEN** an `ERROR:` line about the duplicate ticket identifier is printed and no ticket is created

### Requirement: TICKET CREATE command
The command `TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the existing user with that email, valid only on the given date, priced at the current base ticket price, and print `TICKET CREATED. Price: <price> EUR`. The ticket SHALL reference its user; the user SHALL NOT reference its tickets.

#### Scenario: Ticket at the initial price
- **WHEN** the current system date is 2027-04-15, SYSTEM PRICE has not been used, Ana exists and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** `TICKET CREATED. Price: 40.00 EUR` is printed and ticket `T001` for Ana dated 2027-04-15 exists

#### Scenario: Ticket at a changed price
- **WHEN** `SYSTEM PRICE;45.50` was processed and then `TICKET CREATE;T002;ana@correo.com;2027-04-16`
- **THEN** `TICKET CREATED. Price: 45.50 EUR` is printed, and a later price change does not alter the price of `T002`

#### Scenario: Unknown user or past date
- **WHEN** `TICKET CREATE` names an email of no existing user, or a date earlier than the current system date, or an invalid date
- **THEN** an `ERROR:` line is printed and no ticket is created

### Requirement: TICKET DELETE command
The command `TICKET DELETE;<identifier>` SHALL delete the individual ticket and print `TICKET DELETED` only when its date is strictly later than the current system date. Tickets dated on the current day or earlier SHALL be kept as historical records and the command SHALL be rejected with an `ERROR:` message, as SHALL an unknown identifier or an individual ticket belonging to a group ticket composition.

#### Scenario: Delete a future ticket
- **WHEN** the current system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-16
- **THEN** `TICKET DELETED` is printed and `T002` no longer exists

#### Scenario: Ticket for today cannot be deleted
- **WHEN** the current system date is 2027-04-15 and `TICKET DELETE;T001` is processed for a ticket dated 2027-04-15
- **THEN** an `ERROR:` line is printed and `T001` still exists

# tickets Specification

## Purpose

Defines the Amusement Park tickets: the information and behavior common to every ticket type, the shared identifier space, and the creation and deletion of individual tickets.

## Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and a price, and all ticket types MUST share this information and the price-related behavior through a common abstraction, so operations that do not need the concrete type can handle any ticket.

#### Scenario: Ticket handled through the common abstraction
- **GIVEN** an individual ticket `T001` for `2027-04-15` priced `40.00`
- **WHEN** it is read through the common ticket abstraction
- **THEN** its identifier, date and price are `T001`, `2027-04-15` and `40.00`

### Requirement: Shared ticket identifier space
All ticket types SHALL share one identifier space, checked in a single common way: creating any ticket whose identifier is already used by an existing ticket of any type MUST be rejected with an error and create nothing.

#### Scenario: Duplicate identifier rejected
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;pedro@correo.com;2027-04-16` is processed
- **THEN** an error reporting that the ticket already exists is printed and no ticket is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date and historical otherwise; this status MUST be derived from the ticket date and the current system date, never stored.

#### Scenario: Status follows the system date
- **GIVEN** ticket `T001` dated `2027-04-15`
- **WHEN** the system date is `2027-04-15`, and later `2027-04-16`
- **THEN** the ticket is active on the first date and historical on the second

### Requirement: Create individual ticket
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the existing user with that email, valid only on the given `yyyy-MM-dd` date, priced at the current base price, and print a confirmation that includes the price charged. It MUST be rejected with an error, creating nothing, when the identifier is used, no user has that email, or the date is invalid or earlier than the current system date. The ticket references its user; the user holds no reference to tickets.

#### Scenario: Ticket created at the current price
- **GIVEN** the system date is `2027-04-01`, the base price was set with `SYSTEM PRICE;45.50`, and user `ana@correo.com` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` for Ana on `2027-04-15` priced `45.50` exists and the confirmation shows `45.50`

#### Scenario: Default price
- **GIVEN** `SYSTEM PRICE` was not used in this execution
- **WHEN** a ticket is created
- **THEN** its price is `40.00`

#### Scenario: Ticket for the current day allowed
- **GIVEN** the system date is `2027-04-15`
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2027-04-15` is processed
- **THEN** the ticket is created

#### Scenario: Past date or unknown user rejected
- **GIVEN** the system date is `2027-04-15`
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2027-04-14` or `TICKET CREATE;T004;nadie@correo.com;2027-04-20` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Price change does not alter existing tickets
- **GIVEN** ticket `T001` was created at `40.00`
- **WHEN** `SYSTEM PRICE;50` is processed
- **THEN** `T001` is still priced `40.00`

### Requirement: Delete individual ticket
`TICKET DELETE;<identifier>` SHALL delete the individual ticket only when its date is strictly later than the current system date. Tickets dated on the current day or earlier MUST be kept as historical records and the command rejected with an error, as when no ticket has that identifier.

#### Scenario: Future ticket deleted
- **GIVEN** the system date is `2027-04-01` and ticket `T001` is dated `2027-04-15`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** a confirmation is printed and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **GIVEN** ticket `T001` is dated on or before the current system date
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `T001` still exists

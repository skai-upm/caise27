# individual-ticket-management Specification

## Purpose

Provides the common ticket abstraction with a shared identifier space, plus creation and deletion of individual tickets priced from the current base price.

## Requirements

### Requirement: Shared ticket identifier space
Every ticket SHALL have a unique identifier, a date of use and price information. Individual tickets, group tickets and the individual tickets composing group tickets SHALL share one identifier space, checked in a common way for every ticket type.

#### Scenario: Identifier used by a group ticket
- **WHEN** `TICKET CREATE;TG01;ana@correo.com;2027-04-15` is processed and a group ticket TG01 exists
- **THEN** creation is rejected

#### Scenario: Identifier used by a composition ticket
- **WHEN** `TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-15` is processed and that identifier belongs to the composition of a group ticket
- **THEN** creation is rejected

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, which SHALL be equal to or later than the current system date. Its price SHALL be the current base price, and the application SHALL display the price charged.

#### Scenario: Ticket for today
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed with the default price
- **THEN** ticket T001 is created for Ana and a price of 40.00 is displayed

#### Scenario: Past date
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** creation is rejected

#### Scenario: Unknown user or invalid date
- **WHEN** `TICKET CREATE` names an email of no registered user or a date not in `yyyy-MM-dd` format
- **THEN** creation is rejected and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly later than the current system date. Tickets for the current day or earlier SHALL be retained as historical records. Individual tickets composing a group ticket, and group tickets themselves, SHALL NOT be deleted with `TICKET DELETE`.

#### Scenario: Future ticket deleted
- **WHEN** T003 is dated after the current system date and `TICKET DELETE;T003` is processed
- **THEN** T003 no longer exists

#### Scenario: Ticket for today kept
- **WHEN** T001 is dated today and `TICKET DELETE;T001` is processed
- **THEN** deletion is rejected and T001 still exists

#### Scenario: Composition ticket kept
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a ticket composing group ticket TG01
- **THEN** deletion is rejected and TG01 keeps its full composition

### Requirement: Ticket-to-user direction
An individual ticket SHALL reference the user it was purchased for; users SHALL NOT hold references to their tickets. A user's tickets SHALL be obtained by traversing the tickets managed by the system.

#### Scenario: Ticket knows its user
- **WHEN** ticket T001 is created for Ana
- **THEN** T001 identifies Ana as its user and Ana's representation holds no ticket references

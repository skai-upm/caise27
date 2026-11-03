# individual-ticket-management Specification

## Purpose

Defines the common ticket abstraction, the shared ticket identifier space, and the creation and deletion of individual tickets.

## Requirements

### Requirement: Common ticket abstraction
All tickets SHALL have a unique identifier, a date of use and price information, and individual and group tickets SHALL share common behaviour, particularly price behaviour, through object-oriented inheritance so they can be handled through a common abstraction.

#### Scenario: Common handling
- **WHEN** an operation such as attraction entry receives a ticket identifier
- **THEN** it handles individual and group tickets through the common ticket abstraction

### Requirement: Shared ticket identifier space
All ticket types, including the individual tickets composing group tickets, SHALL share one identifier space checked in a common manner; no two tickets SHALL have the same identifier.

#### Scenario: Identifier already used by a group ticket
- **WHEN** `TICKET CREATE` uses the identifier of an existing group ticket
- **THEN** an error is reported and no ticket is created

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, which SHALL be on or after the current system date; its price SHALL be the current base price and SHALL be displayed. The ticket SHALL reference its user; the user SHALL NOT reference tickets.

#### Scenario: Create ticket for today
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` runs
- **THEN** the ticket is created and its price 40.00 is displayed

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and a ticket for 2027-04-14 is requested
- **THEN** creation is rejected

#### Scenario: Unknown user or invalid date
- **WHEN** `TICKET CREATE` names an unregistered email or a malformed date
- **THEN** an error is reported and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly later than the current system date; tickets for today or earlier SHALL be retained as historical records, and individual tickets composing a group ticket SHALL NOT be deleted independently.

#### Scenario: Delete future ticket
- **WHEN** a standalone ticket dated after the system date is deleted
- **THEN** it is removed

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE` targets a ticket dated today
- **THEN** deletion is rejected

#### Scenario: Ticket inside a group ticket
- **WHEN** `TICKET DELETE;TG01:12345678Z` targets a composition ticket
- **THEN** deletion is rejected

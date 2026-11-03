# individual-ticket-management Specification

## Purpose

Introduces the common ticket abstraction with a shared identifier space and the creation, pricing and deletion of individual tickets, including how tickets restrict user deletion (source F2-C03, sections 7, 7.1, 7.2 and 9).

## Requirements

### Requirement: Common ticket abstraction and identifier space
All tickets SHALL have a unique identifier, a date of use and price information, SHALL share one identifier space checked in a common way (including identifiers of individual tickets inside group tickets), and SHALL be handled through a common abstraction that reuses price behavior.

#### Scenario: Identifier shared between ticket types
- **WHEN** a group ticket `TG01` exists and `TICKET CREATE;TG01;ana@correo.com;<date>` is processed
- **THEN** an error is reported and no ticket is created

### Requirement: Create individual ticket
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, priced at the current base price, and SHALL display that price; it SHALL reject a used identifier, an unknown user, an invalid date or a date earlier than the current system date.

#### Scenario: Ticket for today created with the base price
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed with no `SYSTEM PRICE` used
- **THEN** ticket `T001` is created for Ana and the displayed price is 40.00

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2027-04-15` is processed and no user has that email
- **THEN** an error is reported and no ticket is created

### Requirement: Delete individual ticket
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly later than the current system date; tickets for the current day or earlier SHALL be retained, and individual tickets belonging to a group ticket SHALL NOT be deletable with this command.

#### Scenario: Ticket for today cannot be deleted
- **WHEN** ticket `T001` is dated today and `TICKET DELETE;T001` is processed
- **THEN** an error is reported and the ticket remains

#### Scenario: Future ticket deleted
- **WHEN** ticket `T004` is dated after the current date and `TICKET DELETE;T004` is processed
- **THEN** the ticket is deleted

#### Scenario: Ticket inside a group ticket cannot be deleted directly
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a future group ticket `TG01`
- **THEN** an error is reported and the group ticket and its composition are unchanged

### Requirement: Tickets reference users unidirectionally
An individual ticket SHALL reference the user it was purchased for, while users SHALL NOT hold references to their tickets; a user's tickets SHALL be determined by traversing the tickets managed by the system.

#### Scenario: User tickets found by traversal
- **WHEN** the source code is inspected
- **THEN** the user class has no ticket collection or ticket field, and the individual ticket class holds a user reference

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date and historical otherwise; historical tickets SHALL be retained as records even after their user or group is deleted.

#### Scenario: Historical ticket survives user deletion
- **WHEN** Ana's only ticket is dated before the current date and Ana is deleted
- **THEN** the ticket still exists with its identifier, date, price and holder information

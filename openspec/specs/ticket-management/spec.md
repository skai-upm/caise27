# ticket-management Specification

## Purpose

Defines park tickets: the properties shared by every ticket type (unique identifier in a common identifier space, date of use, price), individual ticket purchase and deletion, and the distinction between active and historical tickets.

## Requirements

### Requirement: Common ticket properties
Every ticket SHALL have an identifier, a date of use and a price, and all ticket types SHALL share one identifier space: no two tickets of any type SHALL have the same identifier.

#### Scenario: Identifier shared across tickets
- **WHEN** ticket `T001` exists and another ticket is created with identifier `T001`
- **THEN** the creation is rejected with an error and no ticket is created

### Requirement: Individual ticket purchase
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the existing user with that email, valid only on the given `yyyy-MM-dd` date, priced at the current base price, and SHALL display the price charged. The ticket SHALL reference its user; the user SHALL NOT reference its tickets.

#### Scenario: Ticket at default price
- **WHEN** the system date is `2027-04-01`, `ana@correo.com` exists and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed without a previous `SYSTEM PRICE`
- **THEN** ticket `T001` for Ana on 2027-04-15 is created and the output shows the price `40.00`

#### Scenario: Ticket at configured price
- **WHEN** `SYSTEM PRICE;45.50` was processed and then `TICKET CREATE;T002;ana@correo.com;2027-04-15`
- **THEN** the output shows the price `45.50`

#### Scenario: Price fixed at creation
- **WHEN** ticket `T001` was created at `40.00` and then `SYSTEM PRICE;45.50` is processed
- **THEN** ticket `T001` still has price `40.00`

#### Scenario: Ticket for the current date
- **WHEN** the system date is `2027-04-15` and `TICKET CREATE;T003;ana@correo.com;2027-04-15` is processed
- **THEN** the ticket is created

#### Scenario: Past date rejected
- **WHEN** the system date is `2027-04-15` and `TICKET CREATE;T004;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user or invalid input rejected
- **WHEN** `TICKET CREATE;T005;nadie@correo.com;2027-04-15`, `TICKET CREATE;T005;ana@correo.com;15/04/2027` or `TICKET CREATE;T005;ana@correo.com` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date. Tickets dated on the current day or earlier SHALL NOT be deleted and SHALL be kept as historical records. Individual tickets that belong to a group ticket SHALL NOT be deleted with `TICKET DELETE`.

#### Scenario: Future ticket deleted
- **WHEN** the system date is `2027-04-01`, ticket `T001` is dated `2027-04-15` and `TICKET DELETE;T001` is processed
- **THEN** a confirmation is printed and `T001` no longer exists

#### Scenario: Ticket for today kept
- **WHEN** the system date is `2027-04-15`, ticket `T001` is dated `2027-04-15` and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `T001` still exists

#### Scenario: Past ticket kept
- **WHEN** ticket `T001` is dated `2027-04-15`, the system date is later set to `2027-04-20` and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is printed

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date and historical otherwise. Ticket ownership SHALL be determined by traversing the tickets managed by the system, not stored on users.

#### Scenario: Ticket becomes historical
- **WHEN** ticket `T001` is dated `2027-04-15` and the system date changes from `2027-04-15` to `2027-04-16`
- **THEN** `T001` changes from active to historical

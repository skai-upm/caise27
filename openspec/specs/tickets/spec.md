# tickets Specification

## Purpose

Defines park tickets common to every ticket type: identity, date, price, active versus historical status, and individual ticket purchase and deletion.

## Requirements

### Requirement: Shared ticket identifier space
Every ticket of any type, including the individual tickets inside group tickets, SHALL have an identifier unique among all tickets in the system. A creation command whose identifier, or any identifier it would derive, is already used SHALL be rejected with an error, and nothing SHALL be created.

#### Scenario: Identifier used by another ticket type
- **WHEN** group ticket `TG01` exists and `TICKET CREATE;TG01;ana@correo.com;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Identifier used by a group ticket component
- **WHEN** group ticket `TG01` includes the ticket `TG01:12345678Z` and `TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-20` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is on or after the current system date, and historical otherwise. A ticket SHALL be valid for attraction access only on its own date.

#### Scenario: Status follows the system date
- **WHEN** a ticket dated `2027-04-15` exists and the system date moves from `2027-04-15` to `2027-04-16`
- **THEN** the ticket changes from active to historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<yyyy-MM-dd>` SHALL create a ticket for an existing user, valid only on the given date and priced at the current base price. It SHALL print `TICKET CREATED` with the charged price. A missing user, past date, invalid date, or used identifier SHALL be rejected with an error.

#### Scenario: Create ticket
- **WHEN** the system date is `2027-04-15`, the price is 40.00, and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** a confirmation including the price `40.00` is printed and ticket `T001` exists for Ana

#### Scenario: Past date rejected
- **WHEN** the system date is `2027-04-15` and `TICKET CREATE;T009;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T009;nobody@correo.com;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly after the current system date, persist the change, and print `TICKET DELETED`. Tickets dated today or earlier, group tickets, and individual tickets inside a group ticket SHALL be rejected with an error.

#### Scenario: Delete future ticket
- **WHEN** the system date is `2027-04-15` and `TICKET DELETE;T002` is processed for a ticket dated `2027-04-20`
- **THEN** `TICKET DELETED` is printed and `T002` no longer exists

#### Scenario: Today's ticket kept
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated on the current system date
- **THEN** an error is printed and `T001` still exists

#### Scenario: Group component cannot be deleted directly
- **WHEN** `TICKET DELETE;TG02:87654321X` is processed for an individual ticket inside group ticket `TG02`
- **THEN** an error is printed and the ticket still exists

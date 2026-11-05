# Spec Delta

## Purpose

Defines park tickets: the common abstraction shared by every ticket type (identifier, date of use, price), the shared identifier space, active versus historical tickets, and the creation and deletion of individual tickets.

## ADDED Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and a price, provided by a common abstraction that all ticket types reuse; operations that do not depend on the concrete type SHALL handle tickets through that abstraction. A ticket SHALL be active when its date is equal to or later than the current system date, and historical otherwise.

#### Scenario: Active and historical tickets
- **WHEN** a ticket dated 2027-04-15 exists and the system date is 2027-04-15
- **THEN** the ticket is active
- **AND** after `SYSTEM DATE;2027-04-16` it is historical

### Requirement: Shared ticket identifier space
All ticket types SHALL share one identifier space: no two tickets of any type may have the same identifier, and this uniqueness SHALL be checked in one common place for every ticket type.

#### Scenario: Duplicate identifier rejected
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;pedro@correo.com;2027-04-20` is processed
- **THEN** an `ERROR` response is printed and no ticket is created

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the existing user with that email, valid only on the given `yyyy-MM-dd` date, priced at the current base price, and print a response starting with `OK` that shows the price. The user MUST exist, the identifier MUST be non-empty and unused, and the date MUST be a valid date not earlier than the current system date; otherwise the command is rejected with an `ERROR` response and no ticket is created. The ticket SHALL reference its user, and the user SHALL NOT store references to tickets.

#### Scenario: Ticket created at the initial price
- **WHEN** the system date is 2027-04-01, no `SYSTEM PRICE` has been used, and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** a response starting with `OK` and containing `40.00` is printed and ticket `T001` for Ana on 2027-04-15 exists

#### Scenario: Ticket created at the configured price
- **WHEN** `SYSTEM PRICE;45.50` was processed and then `TICKET CREATE;T002;ana@correo.com;2027-04-15`
- **THEN** the response contains `45.50` and ticket `T002` has price 45.50

#### Scenario: Price change does not alter existing tickets
- **WHEN** ticket `T001` was created at 40.00 and later `SYSTEM PRICE;50.00` is processed
- **THEN** ticket `T001` still has price 40.00

#### Scenario: Ticket for today allowed
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T003;ana@correo.com;2027-04-15` is processed
- **THEN** the ticket is created

#### Scenario: Past date, unknown user or malformed input rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T004;ana@correo.com;2027-04-14`, `TICKET CREATE;T004;nadie@correo.com;2027-04-20`, `TICKET CREATE;T004;ana@correo.com;2027-02-30` or `TICKET CREATE;;ana@correo.com;2027-04-20` is processed
- **THEN** an `ERROR` response is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete the individual ticket and print a response starting with `OK` only when its date is strictly later than the current system date. Tickets for the current day or earlier MUST NOT be deleted and are retained as historical records. An unknown identifier, or a ticket that belongs to the composition of a group ticket, MUST be rejected with an `ERROR` response.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2027-04-01 and ticket `T001` is dated 2027-04-15
- **THEN** `TICKET DELETE;T001` succeeds and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **WHEN** ticket `T001` is dated 2027-04-15 and the system date is 2027-04-15 or later
- **THEN** `TICKET DELETE;T001` is rejected with an `ERROR` response and the ticket still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an `ERROR` response is printed

### Requirement: Ticket commands listed in HELP
The `HELP` output SHALL list `TICKET CREATE;<id>;<email>;<yyyy-MM-dd>` and `TICKET DELETE;<id>`.

#### Scenario: HELP shows ticket commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both ticket command syntaxes

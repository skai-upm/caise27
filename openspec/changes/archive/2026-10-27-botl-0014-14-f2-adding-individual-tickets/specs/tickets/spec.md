# Spec Delta

## Purpose

Defines park tickets: the information and behaviour shared by all ticket types, the single ticket identifier space, and the creation and deletion of individual tickets.

## ADDED Requirements

### Requirement: Common ticket abstraction
Every ticket type SHALL share one abstraction providing a unique identifier, a date of use, its price and the active/historical rules, so that operations not needing the concrete type handle tickets uniformly.

#### Scenario: Ticket handled through the common abstraction
- **WHEN** the system checks whether a user has active tickets
- **THEN** it traverses all tickets through the common abstraction without distinguishing their concrete type

### Requirement: Shared ticket identifier space
All ticket identifiers SHALL be unique across every ticket type, checked in a single common way.

#### Scenario: Duplicate identifier rejected
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;pedro@correo.com;2027-04-16` is processed
- **THEN** an error about the duplicate identifier is reported and no ticket is created

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for an existing user, valid only on the given date, which SHALL be equal to or later than the current system date; its price SHALL be the current base price, and the response SHALL display that price.

#### Scenario: Ticket at the initial price
- **WHEN** the system date is 2027-04-10, Ana exists and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed without any prior SYSTEM PRICE
- **THEN** the response is `TICKET CREATED - Price: 40.00 EUR` and ticket `T001` references Ana with date 2027-04-15

#### Scenario: Ticket at a changed price
- **WHEN** `SYSTEM PRICE;45.50` was processed and a ticket is created
- **THEN** the response shows `45.50`, and a later `SYSTEM PRICE;50` does not change that ticket's price

#### Scenario: Ticket for today
- **WHEN** the system date is 2027-04-15 and a ticket is created for 2027-04-15
- **THEN** the ticket is created

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Unknown user or bad input rejected
- **WHEN** the email does not belong to a user, the date is not `yyyy-MM-dd`, or fields are missing
- **THEN** an error is reported and no ticket is created

### Requirement: Individual ticket references its user only
An individual ticket SHALL hold a reference to the user for whom it was purchased, and users SHALL NOT hold references to their tickets.

#### Scenario: Unidirectional relationship
- **WHEN** a ticket is created for Ana
- **THEN** the ticket returns Ana as its user and the user type has no ticket attribute

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a ticket only when its date is strictly later than the current system date; tickets for the current day or earlier SHALL be kept as historical records.

#### Scenario: Delete a future ticket
- **WHEN** the system date is 2027-04-10 and ticket `T001` is dated 2027-04-15
- **THEN** `TICKET DELETE;T001` responds `TICKET DELETED` and `T001` no longer exists

#### Scenario: Today's ticket kept
- **WHEN** the system date equals the ticket date
- **THEN** `TICKET DELETE` reports an error and the ticket remains

#### Scenario: Past ticket kept
- **WHEN** the system date is later than the ticket date
- **THEN** `TICKET DELETE` reports an error and the ticket remains

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is reported

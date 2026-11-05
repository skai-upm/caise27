# Spec Delta

## Purpose

Defines park tickets: the information and behavior shared by every ticket type, the single identifier space for all tickets, and the purchase and deletion of individual tickets.

## ADDED Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and a price. Individual and group tickets SHALL share this information and their price-related behavior through a common abstraction, so that operations that do not need the concrete type handle any ticket uniformly.

#### Scenario: Any ticket handled through the common abstraction
- **GIVEN** tickets of the available types
- **WHEN** an operation lists or inspects tickets without knowing their type
- **THEN** it can obtain each ticket's identifier, date and price

### Requirement: Shared ticket identifier space
Ticket identifiers SHALL be unique among all tickets in the system regardless of type, and this uniqueness SHALL be checked in one common way for every ticket type.

#### Scenario: Identifier already used
- **GIVEN** a ticket `T001` exists
- **WHEN** another ticket with identifier `T001` is requested
- **THEN** an error message starting with `ERROR` is printed and no ticket is created

### Requirement: TICKET CREATE command
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the registered user with that email, valid only on `<date>` (`yyyy-MM-dd`), priced at the current base price, and print `TICKET CREATED` together with the price charged. It MUST be rejected with an error message starting with `ERROR` when the identifier is empty or already used, the user does not exist, or the date is invalid or earlier than the current system date. The ticket SHALL reference its user; the user SHALL NOT reference its tickets.

#### Scenario: Ticket at the initial price
- **GIVEN** a registered user `ana@correo.com`, current date 2027-04-01 and no SYSTEM PRICE used
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** `TICKET CREATED` is printed with the price `40.00` and ticket `T001` for Ana on 2027-04-15 exists

#### Scenario: Ticket at the configured price
- **GIVEN** `SYSTEM PRICE;45.50` was processed
- **WHEN** a valid `TICKET CREATE` is processed
- **THEN** the price shown and stored is `45.50`

#### Scenario: Ticket for today allowed
- **GIVEN** current date 2027-04-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2027-04-15` is processed
- **THEN** `TICKET CREATED` is printed

#### Scenario: Past date or unknown user rejected
- **GIVEN** current date 2027-04-15
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2027-04-14` or `TICKET CREATE;T004;nadie@correo.com;2027-04-20` is processed
- **THEN** an error message starting with `ERROR` is printed and no ticket is created

#### Scenario: Later price changes do not alter existing tickets
- **GIVEN** ticket `T001` created at price 40.00
- **WHEN** `SYSTEM PRICE;50` is processed
- **THEN** `T001` still has price 40.00

### Requirement: TICKET DELETE command
`TICKET DELETE;<identifier>` SHALL delete an individual ticket and print `TICKET DELETED` only when its date is strictly later than the current system date. Tickets dated on the current day or earlier MUST NOT be deleted and are retained as historical records; the request is rejected with an error message starting with `ERROR`, as is an unknown identifier.

#### Scenario: Future ticket deleted
- **GIVEN** current date 2027-04-01 and ticket `T001` on 2027-04-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `TICKET DELETED` is printed and `T001` no longer exists

#### Scenario: Ticket for today or the past not deleted
- **GIVEN** ticket `T001` on 2027-04-15
- **WHEN** the current date is 2027-04-15 (or later) and `TICKET DELETE;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;NOPE` is processed
- **THEN** an error message starting with `ERROR` is printed

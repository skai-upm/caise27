# Spec Delta

## Purpose

Defines the common ticket abstraction with its shared identifier space, and individual ticket creation and deletion (F2-C03).

## ADDED Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and price information. Individual and group tickets SHALL be handled through a common abstraction that reuses shared data and price behaviour. A ticket SHALL be active when its date is equal to or later than the current system date, and historical otherwise.

#### Scenario: Active and historical
- **WHEN** the current date is 2027-04-15
- **THEN** tickets dated 2027-04-15 or later are active and tickets dated 2027-04-14 or earlier are historical

### Requirement: Shared ticket identifier space
All tickets — individual tickets, group tickets and the individual tickets composing group tickets — SHALL share one identifier space; creating any ticket whose identifier is already used by any ticket SHALL be rejected.

#### Scenario: Identifier used by a group ticket
- **WHEN** group ticket TG01 exists and `TICKET CREATE;TG01;ana@correo.com;2027-04-15` is processed
- **THEN** an error is reported and no ticket is created

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for an existing user, valid only on the given date, which SHALL be equal to or later than the current system date. Its price SHALL be the current base price, and the application SHALL display that price on success. The ticket SHALL reference its user; the user SHALL NOT reference its tickets.

#### Scenario: Ticket for today
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed with the default price
- **THEN** ticket T001 is created for Ana and a price of 40.00 is displayed

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and a ticket dated 2027-04-14 is requested
- **THEN** an error is reported and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE` is processed with an email that no user has
- **THEN** an error is reported and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date and it is not part of a group ticket. Tickets for the current day or earlier SHALL be retained as historical records. Group tickets SHALL NOT be deletable with this command.

#### Scenario: Future ticket deleted
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a standalone ticket dated 2027-04-16
- **THEN** T002 is deleted

#### Scenario: Ticket for today retained
- **WHEN** `TICKET DELETE` is processed for a ticket dated on the current system date
- **THEN** an error is reported and the ticket is retained

#### Scenario: Constituent of a group ticket
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for an individual ticket composing group ticket TG01
- **THEN** an error is reported and the ticket is retained

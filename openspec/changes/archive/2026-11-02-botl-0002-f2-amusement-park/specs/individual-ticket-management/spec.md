# Spec Delta

## Purpose

Introduces tickets as a common abstraction with a shared identifier space, and individual tickets that a registered user can buy for today or a later date and delete while still in the future.

## ADDED Requirements

### Requirement: Common ticket abstraction and identifier space
Every ticket SHALL have a unique identifier, a date of use and price information; individual and group tickets, including the individual tickets inside group tickets, SHALL share one identifier space checked in a common way. (F2-C03)

#### Scenario: Identifier used by another ticket type
- **WHEN** `TICKET CREATE;TG01;ana@correo.com;<today>` runs and a group ticket TG01 exists
- **THEN** creation is rejected

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for an existing user, with a new identifier and a date equal to or later than the system date, priced at the current base price, and SHALL display that price. The ticket SHALL reference its user; the user SHALL NOT reference its tickets. (F2-C03)

#### Scenario: Create for today
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` runs
- **THEN** the ticket is created and the displayed price is 40.00 euros

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and a ticket for 2027-04-14 is requested
- **THEN** creation is rejected and no ticket exists

#### Scenario: Unknown user or bad date
- **WHEN** `TICKET CREATE` names an unregistered email or an unparseable date
- **THEN** creation is rejected

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly later than the system date; tickets for today or earlier SHALL be kept as historical records, and individual tickets that are part of a group ticket SHALL NOT be deleted with this command. (F2-C03)

#### Scenario: Today's ticket kept
- **WHEN** `TICKET DELETE` targets a ticket dated today
- **THEN** deletion is rejected and the ticket remains

#### Scenario: Future ticket deleted
- **WHEN** `TICKET DELETE` targets a standalone ticket dated after today
- **THEN** the ticket is deleted

#### Scenario: Group component protected
- **WHEN** `TICKET DELETE;TG01:12345678Z` targets an individual ticket inside group ticket TG01
- **THEN** deletion is rejected

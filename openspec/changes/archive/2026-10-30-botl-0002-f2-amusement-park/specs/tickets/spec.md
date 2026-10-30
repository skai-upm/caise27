# Spec Delta

## Purpose

Defines park tickets: the shared identifier space and common behaviour of all ticket types, individual tickets, group tickets with member discounts, and the rules for creating and deleting them.

## ADDED Requirements

### Requirement: Shared ticket identifier space
Every ticket SHALL have an identifier, a date of use and a price. Identifiers SHALL be unique across all ticket types, including identifiers of individual tickets composing group tickets. Uniqueness SHALL be checked by one common mechanism.

#### Scenario: Individual id clashes with group ticket id
- **WHEN** group ticket TG01 exists and `TICKET CREATE;TG01;ana@correo.com;<date>` is processed
- **THEN** an `ERROR:` message is printed and no ticket is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date, and historical otherwise.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the system date is set to 2027-04-16
- **THEN** the ticket is historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, priced at the current base price, and print that price. It SHALL be rejected for an unknown user, a used identifier, an invalid date, or a date before the current system date.

#### Scenario: Ticket created at current price
- **WHEN** the price is 40.00, the date is 2027-04-01 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** the output includes the price 40.00 and ticket T001 exists for ana on 2027-04-15

#### Scenario: Ticket for today allowed, past rejected
- **WHEN** the system date is 2027-04-15 and tickets are requested for 2027-04-15 and 2027-04-14
- **THEN** the first is created and the second is rejected with an `ERROR:` message

#### Scenario: Unknown user
- **WHEN** TICKET CREATE names an email no user has
- **THEN** an `ERROR:` message is printed

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly after the current system date. Tickets for the current day or earlier SHALL be kept. Individual tickets composing a group ticket, and group tickets, SHALL NOT be deleted with this command.

#### Scenario: Future ticket deleted
- **WHEN** T001 is dated after the current date and `TICKET DELETE;T001` is processed
- **THEN** T001 no longer exists

#### Scenario: Current-day ticket kept
- **WHEN** T001 is dated on the current date and `TICKET DELETE;T001` is processed
- **THEN** an `ERROR:` message is printed and T001 still exists

#### Scenario: Composed ticket protected
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a ticket composing group ticket TG01
- **THEN** an `ERROR:` message is printed and the ticket still exists

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create, for an existing non-empty group and a date not before the current date, a group ticket composed of one individual ticket per current member with identifier `<identifier>:<dni>`, referencing the group. If the group ticket identifier or any derived identifier is already used, nothing SHALL be created.

#### Scenario: Composition created
- **WHEN** G01 has members with DNIs 12345678Z and 87654321X and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket TG01 exists, composed of TG01:12345678Z and TG01:87654321X

#### Scenario: Empty group rejected
- **WHEN** GROUPTICKET CREATE names an existing group with no members
- **THEN** an `ERROR:` message is printed and no ticket is created

#### Scenario: Derived identifier clash is atomic
- **WHEN** a ticket TG01:12345678Z already exists and `GROUPTICKET CREATE;TG01;G01;<date>` is processed for a group containing that DNI
- **THEN** an `ERROR:` message is printed and neither TG01 nor any of its composed tickets is created

#### Scenario: Composition is fixed
- **WHEN** TG01 was bought with two members and the group later changes membership
- **THEN** TG01 is still composed of the original two tickets

### Requirement: Group discount and price
Each composed ticket SHALL cost the current base price minus a discount by member count: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket's price SHALL be the sum of its composed tickets' prices and SHALL be printed on creation.

#### Scenario: Six members at 40 euros
- **WHEN** the price is 40.00 and a 6-member group buys a group ticket
- **THEN** each composed ticket costs 32.00 and the printed total is 192.00

#### Scenario: Bracket boundaries
- **WHEN** groups of 2, 3, 5, 10 and 11 members buy at 40.00
- **THEN** totals are 80.00, 108.00, 180.00, 320.00 and 220.00

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all its composed tickets only when its date is strictly after the current system date; otherwise it SHALL be kept as a historical record.

#### Scenario: Future group ticket deleted with composition
- **WHEN** TG01 is dated after the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01 and all `TG01:<dni>` tickets no longer exist

#### Scenario: Current-day group ticket kept
- **WHEN** TG01 is dated on the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an `ERROR:` message is printed and TG01 still exists

# tickets Specification

## Purpose

Defines park tickets: individual tickets for one user and discounted group tickets composed of one individual ticket per group member, their prices, identifiers, and when they may be created or deleted.

## Requirements

### Requirement: Shared ticket identifier space
Every ticket identifier SHALL be unique across all tickets, including individual tickets that belong to group tickets. Creation of any ticket whose identifier (or any derived component identifier) is already in use SHALL be rejected without creating anything.

#### Scenario: Identifier used by a group ticket
- **WHEN** `TICKET CREATE;TG01;ana@correo.com;2027-04-15` is processed and TG01 is a group ticket
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** `GROUPTICKET CREATE;TG03;G01;2027-04-15` is processed and an individual ticket `TG03:12345678Z` already exists for a member of G01
- **THEN** an error is printed and no part of TG03 is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the system date, and historical otherwise.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the system date is set to 2027-04-16
- **THEN** the ticket is historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for the existing user with that email, valid only on the given date, which SHALL be on or after the system date. Its price SHALL be the current base price, and the application SHALL print it.

#### Scenario: Create an individual ticket
- **WHEN** the system date is 2027-04-15, the price is 40.00 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket T001 exists for Ana and a price of 40.00 is printed

#### Scenario: Past date
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T009;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE` names an email that belongs to no user
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a stand-alone individual ticket only when its date is strictly later than the system date. Tickets for the current day or earlier SHALL be kept as historical records. Individual tickets that are part of a group ticket SHALL NOT be deletable with this command.

#### Scenario: Delete a future ticket
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-20
- **THEN** T002 no longer exists

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE` is processed for a ticket dated on the system date
- **THEN** an error is printed and the ticket is kept

#### Scenario: Component of a group ticket
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the ticket is kept

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing non-empty group and a date on or after the system date, create a group ticket composed of one individual ticket per current member, identified `<identifier>:<dni>`, each holding its member. The group ticket SHALL reference the purchasing group, its composition SHALL NOT change afterwards, and the application SHALL print the total price.

#### Scenario: Create a group ticket
- **WHEN** G01 has members Ana (12345678Z), Pedro and Laura and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed on 2027-04-15
- **THEN** TG01 exists with three individual tickets including `TG01:12345678Z`, and its total price is printed

#### Scenario: Empty group
- **WHEN** `GROUPTICKET CREATE` is processed for a group with no members
- **THEN** an error is printed and no ticket is created

#### Scenario: Composition is fixed
- **WHEN** a member is removed from a group after its group ticket has become historical
- **THEN** the group ticket still contains that member's individual ticket

### Requirement: Group discount
Each individual ticket of a group ticket SHALL cost the current base price minus a discount determined by the number of members at purchase: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %. The group ticket price SHALL be the sum of its individual tickets' prices.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is 40.00 and a group of 6 users buys a group ticket
- **THEN** each individual ticket costs 32.00 and a total of 192.00 is printed

#### Scenario: Two members
- **WHEN** the base price is 40.00 and a group of 2 users buys a group ticket
- **THEN** the total is 80.00

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket, together with its individual tickets, only when its date is strictly later than the system date.

#### Scenario: Delete a future group ticket
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** TG02 and all `TG02:<dni>` tickets no longer exist

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE` is processed for a group ticket dated on the system date
- **THEN** an error is printed and the group ticket is kept

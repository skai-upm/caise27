# ticketing Specification

## Purpose

Defines park tickets: individual tickets for one user and group tickets composed of individual tickets for each group member, their pricing, discounts, deletion rules and historical retention.

## Requirements

### Requirement: Shared ticket identifier space
Every ticket identifier, including the derived identifiers of individual tickets inside group tickets, SHALL be unique across all ticket types. A creation whose identifier, or any derived identifier, already exists SHALL be rejected without creating anything.

#### Scenario: Identifier used by another type
- **WHEN** individual ticket TG01 exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** an error is printed and no group ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** an individual ticket `TG05:12345678Z` exists and `GROUPTICKET CREATE;TG05;G01;2027-04-15` is processed for a group containing the user with DNI 12345678Z
- **THEN** an error is printed and no part of TG05 is created

### Requirement: Ticket activity
A ticket SHALL be active when its date is on or after the current system date and historical otherwise.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the system date is set to 2027-04-16
- **THEN** the ticket is historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, which SHALL be on or after the current system date. Its price SHALL be the current base price, and the application SHALL display that price on success.

#### Scenario: Create ticket
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** T001 is created for Ana and the output shows a price of 40.00

#### Scenario: Past date
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T009;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE;T010;nadie@correo.com;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly after the current system date. Tickets for the current day or earlier SHALL be kept as historical records. Individual tickets that belong to a group ticket SHALL NOT be deletable with `TICKET DELETE`.

#### Scenario: Delete future ticket
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-20
- **THEN** T002 is deleted

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated the current system date
- **THEN** an error is printed and T001 is kept

#### Scenario: Component of a group ticket
- **WHEN** `TICKET DELETE;TG02:87654321X` is processed
- **THEN** an error is printed and the group ticket is unchanged

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing non-empty group and a date on or after the current system date, create a group ticket composed of one individual ticket per current member with identifier `<identifier>:<dni>`. The composition SHALL NOT change afterwards. The application SHALL display the total price on success.

#### Scenario: Create group ticket
- **WHEN** G01 has members with DNIs 12345678Z, 87654321X and 11111111H and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** TG01 is created with individual tickets `TG01:12345678Z`, `TG01:87654321X` and `TG01:11111111H`

#### Scenario: Empty group
- **WHEN** `GROUPTICKET CREATE;TG03;G03;2027-04-15` is processed and G03 has no members
- **THEN** an error is printed and no ticket is created

#### Scenario: Composition is fixed
- **WHEN** TG01 is historical and a member is later removed from G01
- **THEN** TG01 still contains the removed member's individual ticket

### Requirement: Group discount
Each individual ticket in a group ticket SHALL cost the current base price minus a discount determined by the number of members at purchase time: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %. The group ticket total SHALL be the sum of its individual ticket prices.

#### Scenario: Six members
- **WHEN** the base price is 40.00 and a group of 6 users creates a group ticket
- **THEN** each individual ticket costs 32.00 and the displayed total is 192.00

#### Scenario: Three members
- **WHEN** the base price is 40.00 and a group of 3 users creates a group ticket
- **THEN** the displayed total is 108.00

#### Scenario: Two members
- **WHEN** the base price is 40.00 and a group of 2 users creates a group ticket
- **THEN** the displayed total is 80.00

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket, together with its individual tickets, only when its date is strictly after the current system date.

#### Scenario: Delete future group ticket
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** TG02 and its individual tickets no longer exist

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated the current system date
- **THEN** an error is printed and TG01 is kept

### Requirement: Historical records survive deletions
Deleting a user or a group SHALL NOT remove historical tickets. A historical ticket SHALL keep its identifier, date, price and composition (including the holder's identity) even when its user or group no longer exists.

#### Scenario: Group deleted
- **WHEN** group G01 with historical group ticket TG01 is deleted
- **THEN** TG01 still exists with its date, price and individual tickets and no longer references a group

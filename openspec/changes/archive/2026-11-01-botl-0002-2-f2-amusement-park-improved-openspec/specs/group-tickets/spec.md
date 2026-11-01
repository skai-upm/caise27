# Spec Delta

## Purpose

Lets a group buy one ticket for a date that is composed of one individual ticket per member, priced with size-based discounts, locking the group while it is current.

## ADDED Requirements

### Requirement: Group ticket composition
A group ticket SHALL reference the group it was bought for and SHALL own one constituent individual ticket per group member at purchase time; each constituent ticket SHALL reference its user. Later membership changes SHALL NOT alter an existing group ticket.

#### Scenario: One ticket per member
- **GIVEN** group `G01` with three members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is executed
- **THEN** `TG01` contains three constituent tickets, one for each member, all dated 2026-10-15

### Requirement: Common ticket abstraction
Individual and group tickets SHALL share a common ticket abstraction exposing identifier, date, price and the users the ticket admits, so operations such as attraction entry and active-ticket checks treat both types uniformly.

#### Scenario: Uniform handling
- **GIVEN** an individual ticket and a group ticket valid for the system date
- **WHEN** each is used to enter an attraction
- **THEN** both are accepted through the same entry operation

### Requirement: Group discount pricing
The group ticket price SHALL be the base price at purchase multiplied by the number of members, reduced by 0% for fewer than 3 members, 10% for 3–5, 20% for 6–10 and 50% for more than 10, rounded to two decimals.

#### Scenario: Discount boundaries
- **GIVEN** a base price of 40.00
- **WHEN** groups of 2, 3, 5, 6, 10 and 11 members buy group tickets
- **THEN** the prices are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00 respectively

### Requirement: Create group ticket command
`GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd` SHALL create the group ticket and its constituents for a date on or after the system date, replying `OK GROUP TICKET CREATED -> <Precio>` with two decimals. An unknown or empty group, a malformed or past date, or an identifier already used by any ticket (including generated constituent identifiers) SHALL be rejected without creating any ticket.

#### Scenario: Successful group purchase
- **GIVEN** base price 40.00 and group `G01` with 3 members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is executed on system date 2026-10-01
- **THEN** the reply is `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Failed purchase creates nothing
- **WHEN** `GROUPTICKET CREATE` is rejected
- **THEN** neither the group ticket nor any constituent ticket exists

### Requirement: Group locking
While a group has a group ticket dated on or after the system date, adding or removing members and deleting the group SHALL be rejected, and its members SHALL NOT be deletable. The lock SHALL be derived from the current system date, not stored.

#### Scenario: Locked and unlocked group
- **GIVEN** group `G01` with group ticket dated 2026-10-15 and system date 2026-10-15
- **WHEN** `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE` is executed for `G01`
- **THEN** the command is rejected; after `SYSTEM DATE;2026-10-16` the same commands are allowed

### Requirement: Delete group ticket command
`GROUPTICKET DELETE;IdTicket` SHALL delete a group ticket only if its date is strictly later than the system date, removing all its constituent tickets in the same operation. Otherwise the state SHALL remain unchanged.

#### Scenario: All-or-nothing group-ticket deletion
- **GIVEN** a future group ticket with several constituent tickets
- **WHEN** it is deleted
- **THEN** the group ticket and all its constituents are removed

#### Scenario: Current group ticket not deletable
- **GIVEN** a group ticket dated on the system date
- **WHEN** `GROUPTICKET DELETE` names it
- **THEN** the command is rejected and all its tickets remain

### Requirement: Group ticket persistence
Group tickets SHALL be persisted with their group reference, price and constituent tickets, and restored at startup.

#### Scenario: Group ticket survives restart
- **GIVEN** group ticket `TG01` with three constituents
- **WHEN** the application restarts
- **THEN** `TG01` exists with the same price, group and three constituents referencing the same users

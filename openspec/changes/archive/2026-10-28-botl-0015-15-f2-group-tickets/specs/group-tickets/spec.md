# Spec Delta

## Purpose

Defines group tickets: joint tickets bought by a non-empty group for all its current members, composed of individual tickets with derived identifiers, priced with a discount based on the number of members, and governing the group's lock while active.

## ADDED Requirements

### Requirement: GROUPTICKET CREATE command
The command `GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for the existing group, dated on the given day, composed of one new individual ticket per user belonging to the group at that moment, and print `GROUPTICKET CREATED. Total price: <total> EUR`. The group ticket SHALL reference the purchasing group; the group SHALL NOT reference its tickets.

#### Scenario: Purchase for a group
- **WHEN** the current date is 2027-04-15, group `G01` contains Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`), and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** `GROUPTICKET CREATED. Total price: 80.00 EUR` is printed and `TG01` consists of individual tickets `TG01:12345678Z` for Ana and `TG01:87654321X` for Pedro dated 2027-04-15

#### Scenario: Rejected purchases
- **WHEN** the group does not exist, the group is empty, or the date is earlier than the current system date
- **THEN** an `ERROR:` line is printed and no ticket is created

### Requirement: Derived identifiers in the shared space
Each individual ticket of a group ticket SHALL have the identifier `<groupTicketIdentifier>:<dni>`. The group ticket identifier and all derived identifiers SHALL be checked against all existing tickets before anything is created; on any conflict the group ticket SHALL NOT be created, not even partially.

#### Scenario: Derived identifier conflict
- **WHEN** an individual ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing DNI `12345678Z`
- **THEN** an `ERROR:` line about the identifier is printed and neither `TG01` nor any of its individual tickets exists

### Requirement: Group discounts
The price of each individual ticket of a group ticket SHALL be the current base price minus a discount depending on the number of members included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of its individual ticket prices.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is 40.00 and a group of 6 users buys a group ticket
- **THEN** each of the six individual tickets costs 32.00 and `GROUPTICKET CREATED. Total price: 192.00 EUR` is printed

#### Scenario: Bracket boundaries
- **WHEN** groups of 2, 3, 5, 10 and 11 members buy group tickets at a base price of 40.00
- **THEN** the individual prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: Fixed composition
The composition of a group ticket SHALL NOT change after purchase, even if the group's members change later or the group is deleted.

#### Scenario: Group changes after a historical ticket
- **WHEN** `TG01` was bought for Ana and Pedro, its date is now in the past and Pedro is removed from `G01`
- **THEN** `TG01` still consists of the tickets for Ana and Pedro

### Requirement: GROUPTICKET DELETE command
The command `GROUPTICKET DELETE;<identifier>` SHALL delete the group ticket together with all individual tickets of its composition and print `GROUPTICKET DELETED`, only when its date is strictly later than the current system date. Group tickets for the current day or earlier SHALL be kept as historical records and the command SHALL be rejected with an `ERROR:` message, as SHALL an identifier that is not a group ticket.

#### Scenario: Delete a future group ticket
- **WHEN** the current date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** `GROUPTICKET DELETED` is printed and neither `TG02` nor its individual tickets exist

#### Scenario: Group ticket for today
- **WHEN** the current date is 2027-04-15 and `GROUPTICKET DELETE;TG01` is processed for a group ticket dated 2027-04-15
- **THEN** an `ERROR:` line is printed and `TG01` still exists

### Requirement: Composition tickets are not deleted individually
`TICKET DELETE` SHALL be rejected with an `ERROR:` message for an individual ticket that belongs to a group ticket and for a group ticket identifier.

#### Scenario: Delete a composition ticket directly
- **WHEN** `TICKET DELETE;TG02:12345678Z` is processed for a ticket in the composition of `TG02`
- **THEN** an `ERROR:` line is printed and the ticket still exists

### Requirement: Active group tickets lock the group
A group SHALL be locked while at least one of its group tickets is dated on or after the current system date: `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` are then rejected, and so is `USER DELETE` of any member. Once all its group tickets are historical or deleted, the group SHALL be modifiable again.

#### Scenario: Locked until the ticket is deleted
- **WHEN** `G01` has a future group ticket `TG02`, `GROUP ADD;G01;luis@correo.com` is rejected, then `GROUPTICKET DELETE;TG02` succeeds and no other active group ticket of `G01` remains
- **THEN** `GROUP ADD;G01;luis@correo.com` succeeds

#### Scenario: Unlocked once historical
- **WHEN** `G01`'s only group ticket is dated 2027-04-15 and the system date becomes 2027-04-16
- **THEN** `GROUP REMOVE` on `G01` succeeds

#### Scenario: Member of a locked group cannot be deleted
- **WHEN** Pedro has no individual tickets but belongs to `G01`, which has an active group ticket including him
- **THEN** `USER DELETE` for Pedro is rejected with an `ERROR:` line

### Requirement: Historical group tickets survive group deletion
When a group is deleted, its historical group tickets SHALL be kept with their identifier, date, price and composition, and their reference to the group SHALL become empty.

#### Scenario: Delete a group with only historical tickets
- **WHEN** `G01`'s only group ticket `TG01` is in the past and `GROUP DELETE;G01` is processed
- **THEN** `GROUP DELETED` is printed and `TG01` still exists with no group reference and its original composition and price

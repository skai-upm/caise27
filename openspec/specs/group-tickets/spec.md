# group-tickets Specification

## Purpose

Lets a group buy one ticket for all its current members at a size-based discount, keeps the purchased composition, and protects the group and its members while that ticket is current or future.

## Requirements

### Requirement: Group ticket composition
A group ticket SHALL have an identifier, a date, a reference to the group that bought it, and SHALL be composed of one individual ticket per member of the group at the moment of purchase. Each constituent individual ticket SHALL have the group ticket's date, refer to its member user, and store the discounted per-member price: the individual price in force at purchase reduced by the group's discount percentage (two decimals, half up). Later changes to the group's membership SHALL NOT change an existing group ticket's composition.

#### Scenario: One ticket per member
- **GIVEN** group `G01` has members Ana, Pedro and Luis
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** `TG01` contains exactly three individual tickets dated 2026-10-15, one for each of Ana, Pedro and Luis

#### Scenario: Composition is a snapshot
- **GIVEN** `TG01` was bought for `G01` with three members and its date has passed
- **WHEN** a member is removed from `G01`
- **THEN** `TG01` still contains three individual tickets

#### Scenario: Stored per-member price
- **GIVEN** the individual price is 40.00 and `G01` has 3 members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** each constituent ticket stores 36.00, while the group ticket's price is 108.00

### Requirement: Create group ticket
The system SHALL process `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd`. It SHALL create the group ticket only if the identifier is non-blank and not used by any ticket, the group exists and has at least one member, and the date is a valid `yyyy-MM-dd` date on or after the system date. On success it SHALL print exactly `OK GROUP TICKET CREATED -> <Precio>`, with the total price formatted with two decimals and `.` as separator. Otherwise it SHALL print a line starting with `ERROR` and create nothing.

#### Scenario: Successful purchase
- **GIVEN** the system date is 2026-10-01, the price is 40.00 and `G01` has 3 members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** it prints `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Invalid purchase
- **WHEN** the group does not exist, the group has no members, the date is before the system date or not a valid `yyyy-MM-dd` date, the identifier is blank or already used by an individual or group ticket, or the parameter count is not three
- **THEN** the result starts with `ERROR` and no group or individual ticket is created

### Requirement: Group discount brackets
The group ticket price SHALL be the individual price multiplied by the number of members, reduced by 0% for fewer than 3 members, 10% for 3 to 5 members, 20% for 6 to 10 members, and 50% for more than 10 members, rounded to two decimals (half up).

#### Scenario: Prices at the bracket boundaries
- **GIVEN** the individual price is 40.00
- **WHEN** group tickets are bought for groups of 2, 3, 5, 6, 10 and 11 members
- **THEN** the prices are 80.00 (0%), 108.00 (10%), 180.00 (10%), 192.00 (20%), 320.00 (20%) and 220.00 (50%)

#### Scenario: One member
- **GIVEN** the individual price is 40.00
- **WHEN** a group with one member buys a group ticket
- **THEN** the price is 40.00

### Requirement: Delete group ticket
The system SHALL process `GROUPTICKET DELETE;Id`, deleting the group ticket and all its constituent individual tickets only if it exists and its date is later than the system date. Otherwise it SHALL print a line starting with `ERROR` and keep everything.

#### Scenario: Delete a future group ticket
- **GIVEN** the system date is 2026-10-01 and `TG001` is dated 2026-10-15
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** the result starts with `OK`, and `TG001` and its individual tickets no longer exist

#### Scenario: Current, past or unknown group ticket
- **WHEN** `GROUPTICKET DELETE;TG001` is processed with the system date equal to or after the ticket date, or no group ticket `TG001` exists
- **THEN** the result starts with `ERROR` and nothing is deleted

### Requirement: Group locked by current or future group tickets
While a group has a group ticket whose date is on or after the system date (`ticketDate >= SYSTEM DATE`), `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on that group SHALL be rejected with `ERROR` without changes. Once all its group tickets are dated before the system date, these commands SHALL work as usual.

#### Scenario: Locked group
- **GIVEN** the system date is 2026-10-01 and `G01` has group ticket `TG01` dated 2026-10-15
- **WHEN** `GROUP ADD;G01;luis@correo.com`, `GROUP REMOVE;G01;ana@correo.com` or `GROUP DELETE;G01` is processed, with the system date 2026-10-01 or 2026-10-15
- **THEN** each result starts with `ERROR` and `G01` is unchanged

#### Scenario: Unlocked when tickets are in the past
- **GIVEN** `G01`'s only group ticket is dated 2026-10-15
- **WHEN** `SYSTEM DATE;2026-10-16` is processed and then `GROUP REMOVE;G01;ana@correo.com`
- **THEN** the removal succeeds

### Requirement: Constituent tickets belong to their group ticket
The individual tickets generated by a group ticket SHALL count as tickets of their users for every rule that considers a user's tickets, and SHALL NOT be deletable on their own with `TICKET DELETE`; they are deleted only through `GROUPTICKET DELETE`. Individual, constituent and group ticket identifiers SHALL be unique across all of them, and `TICKET DELETE` SHALL NOT delete a group ticket nor `GROUPTICKET DELETE` an individual ticket.

#### Scenario: Deleting a constituent directly
- **GIVEN** `TG01` contains the individual ticket generated for Ana
- **WHEN** `TICKET DELETE` is processed with that individual ticket's identifier
- **THEN** the result starts with `ERROR` and `TG01` is unchanged

#### Scenario: Wrong command for the ticket kind
- **WHEN** `TICKET DELETE;TG01` is processed for group ticket `TG01`, or `GROUPTICKET DELETE;T001` for individual ticket `T001`
- **THEN** the result starts with `ERROR` and nothing is deleted

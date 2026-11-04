# group-tickets Specification

## Purpose
Lets a group buy one ticket for all its current members, with a discount based on the number of members, and keeps the group and its members stable while that purchase is current or future.

## Requirements

### Requirement: Common ticket abstraction
Individual tickets and group tickets SHALL be handled through a common ticket abstraction exposing identifier, date of use and price; ticket identifiers SHALL be unique across both kinds.

#### Scenario: Identifier shared space
- **GIVEN** an individual ticket `T001` exists
- **WHEN** `GROUPTICKET CREATE;T001;G01;2026-10-15` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: Group ticket composition
A group ticket SHALL reference (aggregate) the group that bought it and SHALL be composed of exactly one individual ticket per member of the group at the moment of purchase; each constituent ticket SHALL reference its user, have the group ticket's date and the individual price used for the purchase, and be identified `<group ticket id>-<n>`.

#### Scenario: One ticket per member
- **GIVEN** group `G01` with members Ana, Pedro and Luis, system date 2026-10-01 and price 40.00
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** individual tickets `TG01-1`, `TG01-2`, `TG01-3` dated 2026-10-15 at 40.00 exist for Ana, Pedro and Luis respectively, and `TG01` is composed of them

#### Scenario: Composition frozen at purchase
- **GIVEN** `TG01` was bought for `G01` with three members and its date has passed
- **WHEN** a member is later added to or removed from `G01`
- **THEN** `TG01` still has the same three constituent tickets

### Requirement: Group ticket purchase
The system SHALL process `GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd` when the identifier and all generated constituent identifiers are unused, the group exists and has at least one member, and the date is a valid `yyyy-MM-dd` date not earlier than the system date; on success it SHALL answer exactly `OK GROUP TICKET CREATED -> <Precio>` with the discounted total written with two decimals.

#### Scenario: Successful purchase
- **GIVEN** group `G01` with 3 members and individual price 40.00
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** the response is `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Rejected purchases create nothing
- **WHEN** the group does not exist, the group has no members, the date is before the system date, the date is invalid, or the identifier is already used
- **THEN** an `ERROR:` response is printed and no group or individual ticket is created

### Requirement: Discount by number of members
The group ticket total SHALL be the individual price multiplied by the number of members, reduced by 0% for fewer than 3 members, 10% for 3 to 5, 20% for 6 to 10 and 50% for more than 10, rounded to two decimals.

#### Scenario: Source examples at 40.00
- **WHEN** groups of 2, 3, 6 and 11 members buy a group ticket at individual price 40.00
- **THEN** the totals are 80.00, 108.00, 192.00 and 220.00

#### Scenario: Bracket boundaries
- **WHEN** groups of 1, 2, 3, 5, 6, 10 and 11 members buy at 40.00
- **THEN** the discounts applied are 0%, 0%, 10%, 10%, 20%, 20% and 50% (totals 40.00, 80.00, 108.00, 180.00, 192.00, 320.00, 220.00)

#### Scenario: Configured price is the base
- **GIVEN** `SYSTEM PRICE;60.00` was processed
- **WHEN** a group of 3 buys a group ticket
- **THEN** the response is `OK GROUP TICKET CREATED -> 162.00`

### Requirement: Group ticket deletion
The system SHALL process `GROUPTICKET DELETE;Identificador`, deleting the group ticket together with all its constituent individual tickets only if it exists and its date is strictly later than the system date.

#### Scenario: Future group ticket deleted with its tickets
- **GIVEN** `TG001` dated 2026-10-15 with three constituent tickets and system date 2026-10-01
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** an `OK:` response is printed and `TG001` and its three individual tickets no longer exist

#### Scenario: Current or past group ticket kept
- **WHEN** the system date is on or after the group ticket's date and `GROUPTICKET DELETE` is processed for it
- **THEN** an `ERROR:` response is printed and nothing is deleted

#### Scenario: Unknown group ticket
- **WHEN** `GROUPTICKET DELETE;TG999` is processed and it does not exist
- **THEN** an `ERROR:` response is printed

### Requirement: Group locked by current or future group tickets
While a group has any group ticket with `ticketDate >= SYSTEM DATE`, adding or removing members and deleting the group SHALL be rejected; once all its group tickets are dated before the system date, these operations SHALL be allowed again.

#### Scenario: Locked group
- **GIVEN** `G01` bought `TG01` dated 2026-10-15 and the system date is 2026-10-15
- **WHEN** `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE` is processed for `G01`
- **THEN** an `ERROR:` response is printed and the group is unchanged

#### Scenario: Lock released when the date passes
- **GIVEN** `G01`'s only group ticket is dated 2026-10-15
- **WHEN** `SYSTEM DATE;2026-10-16` is processed and then a member is added
- **THEN** the member is added

### Requirement: Constituent tickets block user deletion
A user SHALL NOT be deleted while any constituent ticket of theirs is dated on or after the system date, exactly as for individual tickets.

#### Scenario: Member with a future group ticket
- **GIVEN** Ana is in `G01`, which bought `TG01` dated 2026-10-15, and the system date is 2026-10-01
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` response is printed and Ana is still registered

### Requirement: Group ticket commands in HELP
The `HELP` response SHALL include `GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd` and `GROUPTICKET DELETE;Identificador`.

#### Scenario: Help shows group ticket commands
- **WHEN** `HELP` is processed
- **THEN** the response contains both lines

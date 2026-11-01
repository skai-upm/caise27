# group-tickets Specification

## Purpose

Lets a group buy one ticket for all its current members, composed of one individual ticket per member, priced with size-based discounts and managed through the same ticket abstraction as individual tickets.

## Requirements

### Requirement: Group ticket composition
A group ticket SHALL have a unique identifier, a date, a total price, a reference to the group that bought it, and SHALL own one constituent individual ticket per group member at purchase time, each referencing its user. Later membership changes SHALL NOT alter an existing group ticket.

#### Scenario: One constituent ticket per member
- **GIVEN** group `G01` with three members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** `TG01` contains three individual tickets dated 2026-10-15, one for each member

#### Scenario: Composition captured at purchase
- **GIVEN** group ticket `TG01` bought for `G01` with members Ana and Pedro, whose date is now in the past
- **WHEN** a new member is added to `G01`
- **THEN** `TG01` still contains only Ana's and Pedro's tickets

### Requirement: Common ticket abstraction
Individual and group tickets SHALL share a common abstraction exposing identifier, date, price, and the users the ticket admits, so that operations applying to any ticket (such as attraction entry) handle both types uniformly.

#### Scenario: Uniform holder access
- **GIVEN** an individual ticket for Ana and a group ticket for Ana and Pedro
- **WHEN** the users admitted by each ticket are requested through the common abstraction
- **THEN** the individual ticket yields Ana and the group ticket yields Ana and Pedro

### Requirement: Group discount pricing
The group ticket price SHALL be the base price at purchase multiplied by the number of members, minus a discount of 0% for fewer than 3 members, 10% for 3–5, 20% for 6–10, and 50% for more than 10, rounded to two decimals. Each constituent ticket SHALL record the discounted per-member price.

#### Scenario: Discount brackets
- **GIVEN** a base price of `40.00`
- **WHEN** groups of 2, 3, 6, and 11 members buy group tickets
- **THEN** the totals are `80.00`, `108.00`, `192.00`, and `220.00`

#### Scenario: Bracket boundaries
- **GIVEN** a base price of `40.00`
- **WHEN** groups of 5, 6, 10, and 11 members buy group tickets
- **THEN** the totals are `180.00`, `192.00`, `320.00`, and `220.00`

### Requirement: GROUPTICKET CREATE command
`GROUPTICKET CREATE;<Id>;<GrupoId>;<yyyy-MM-dd>` SHALL create a group ticket and report exactly `OK GROUP TICKET CREATED -> <Precio>` with the total formatted with two decimals. It SHALL be rejected, creating no ticket at all, when the group does not exist or has no members, the date is invalid or earlier than the system date, or the identifier or any constituent identifier is already used by a ticket.

#### Scenario: Create group ticket
- **GIVEN** system date 2026-10-01, base price `40.00`, and group `G01` with three members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** the output is `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Failed purchase creates nothing
- **GIVEN** an empty group `G02`
- **WHEN** `GROUPTICKET CREATE;TG02;G02;2026-10-15` is processed
- **THEN** an error message is reported and neither a group ticket nor any constituent ticket exists

### Requirement: Group tickets block member deletion
Each constituent ticket SHALL block deletion of its user while dated on or after the system date, exactly like an individual ticket.

#### Scenario: Member cannot be deleted
- **GIVEN** system date 2026-10-01 and group ticket `TG01` dated 2026-10-15 including Ana
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message is reported and Ana remains registered

### Requirement: GROUPTICKET DELETE command
`GROUPTICKET DELETE;<Id>` SHALL delete a group ticket together with all its constituent tickets only when its date is strictly later than the system date; otherwise, or for unknown identifiers, it SHALL be rejected leaving every ticket unchanged.

#### Scenario: All-or-nothing group-ticket deletion
- **GIVEN** system date 2026-10-01 and group ticket `TG001` dated 2026-10-15 with three constituent tickets
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** the group ticket and its three constituent tickets are deleted

#### Scenario: Today or past group ticket is kept
- **GIVEN** system date 2026-10-15 and group ticket `TG01` dated 2026-10-15
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message is reported and the group ticket and all its constituents remain

### Requirement: Group ticket persistence
Group tickets SHALL be persisted with their group reference, total price, and constituent tickets, and restored at startup, including as history when the group or members were later deleted.

#### Scenario: Group ticket survives restart
- **GIVEN** group ticket `TG01` with constituents for Ana and Pedro
- **WHEN** the application restarts
- **THEN** `TG01` exists with the same group, date, total price, and constituents referencing Ana and Pedro

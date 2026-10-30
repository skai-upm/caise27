# group-tickets Specification

## Purpose

Lets a group buy a ticket for all its current members at once, composed of one individual ticket per member and discounted by group size.

## Requirements

### Requirement: Group ticket composition
A group ticket SHALL reference its group, have an identifier, a date, a total price, and own one individual ticket per group member at purchase time. Constituent tickets SHALL reference their users and SHALL be identified as `<groupTicketId>-<n>`, with `n` from 1 in membership order. Group and individual tickets SHALL share a common ticket abstraction.

#### Scenario: One ticket per member
- **GIVEN** group `G01` with three members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is executed
- **THEN** group ticket `TG01` contains tickets `TG01-1`, `TG01-2`, `TG01-3` for those members

#### Scenario: Composition fixed at purchase
- **GIVEN** group ticket `TG01` bought for three members of a group whose tickets are now past
- **WHEN** a new member is added to the group
- **THEN** `TG01` still contains three tickets

### Requirement: Group ticket pricing
The total price SHALL be the base price multiplied by the number of members with a discount of 0 % for fewer than 3 members, 10 % for 3–5, 20 % for 6–10 and 50 % for more than 10, rounded to two decimals. Each constituent ticket SHALL record the discounted per-person price.

#### Scenario: Discount brackets
- **GIVEN** base price 40.00
- **WHEN** groups of 2, 3, 5, 6, 10 and 11 members buy group tickets
- **THEN** the totals are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00

### Requirement: Create group ticket
`GROUPTICKET CREATE;<id>;<groupId>;<yyyy-MM-dd>` SHALL, for an existing non-empty group and a date on or after the system date, create the group ticket and print exactly `OK GROUP TICKET CREATED -> <Precio>`. If any check fails, including identifier collisions with existing tickets, nothing SHALL be created.

#### Scenario: Successful purchase
- **GIVEN** base price 40.00 and group `G01` with 3 members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is executed on system date 2026-10-01
- **THEN** the output is `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Failed purchase creates nothing
- **WHEN** the group is unknown or empty, the date is past or malformed, or an identifier is already used
- **THEN** an error is reported and no group ticket nor constituent ticket exists

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<id>` SHALL, only when the group ticket's date is strictly after the system date, delete the passes of its constituent tickets, then the constituent tickets, then the group ticket, as one operation; otherwise nothing SHALL be deleted.

#### Scenario: All-or-nothing deletion
- **GIVEN** system date 2026-10-01 and group ticket `TG01` dated 2026-10-15 with three constituents
- **WHEN** `GROUPTICKET DELETE;TG01` is executed
- **THEN** `TG01` and its three constituent tickets no longer exist

#### Scenario: Deletion rejected on the ticket date
- **GIVEN** system date equal to the date of `TG01`
- **WHEN** `GROUPTICKET DELETE;TG01` is executed
- **THEN** an error is reported and `TG01` with all constituents remains

#### Scenario: Constituent passes deleted
- **GIVEN** a future group ticket whose constituents have SpeedPasses
- **WHEN** it is deleted
- **THEN** those SpeedPasses no longer exist

### Requirement: Group ticket persistence
Group tickets, their constituent tickets and their group reference SHALL be stored in a text file and restored at startup.

#### Scenario: Restore group ticket
- **GIVEN** group ticket `TG01` with three constituents
- **WHEN** the application restarts
- **THEN** `TG01` exists with the same group, date, total price and constituents referencing the same users

# group-tickets Specification

## Purpose

Lets a group buy a single ticket for all its current members with a size-based discount, and locks the group while that ticket is current or future.

## Requirements

### Requirement: Group ticket composition
A group ticket SHALL reference the group that bought it and SHALL own one constituent individual ticket per group member at purchase time; each constituent SHALL reference its member user. Later membership changes SHALL NOT alter an existing group ticket. Constituent identifiers SHALL be `<groupTicketId>-<n>` with `n` from 1 in member order.

#### Scenario: One constituent per member
- **WHEN** group `G01` has Ana and Pedro and `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** `TG01` owns tickets `TG01-1` for Ana and `TG01-2` for Pedro, both dated 2026-10-15

#### Scenario: Composition captured at purchase
- **WHEN** `TG01` was bought for Ana and Pedro and its date becomes past, after which Luis is added to `G01`
- **THEN** `TG01` still has exactly the constituents for Ana and Pedro

### Requirement: Group ticket pricing
The total price SHALL be the current base price multiplied by the number of members, reduced by 0% for fewer than 3 members, 10% for 3 to 5, 20% for 6 to 10 and 50% for more than 10. Each constituent's price SHALL be the total divided by the member count.

#### Scenario: Discount brackets
- **WHEN** the base price is 40.00 and groups of 2, 3, 5, 6, 10 and 11 members buy group tickets
- **THEN** the totals are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00 respectively

### Requirement: GROUPTICKET CREATE command
`GROUPTICKET CREATE;<id>;<groupId>;<yyyy-MM-dd>` SHALL create the group ticket and print `OK GROUP TICKET CREATED -> <Precio>` with two decimals. It SHALL be rejected without creating any ticket if the group does not exist or has no members, the date is invalid or earlier than the system date, or the group-ticket identifier or any constituent identifier is already used.

#### Scenario: Successful purchase
- **WHEN** group `G01` has three members, the base price is 40.00 and `GROUPTICKET CREATE;TG01;G01;2026-10-15` is executed on or before 2026-10-15
- **THEN** the output is `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Failed purchase creates nothing
- **WHEN** `GROUPTICKET CREATE` names an unknown or empty group or an already used identifier
- **THEN** an error is reported and no group or constituent ticket exists afterwards

### Requirement: Group locking
While a group has any group ticket dated on or after the system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on that group SHALL be rejected. The lock SHALL be derived from the current system date and not stored. Constituent tickets SHALL block deletion of their users like any individual ticket.

#### Scenario: Locked group
- **WHEN** `G01` has a group ticket dated 2026-10-15 and the system date is 2026-10-15
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are rejected, and `USER DELETE` for any member is rejected

#### Scenario: Lock released when tickets become historical
- **WHEN** the system date is moved to 2026-10-16
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are accepted again

### Requirement: GROUPTICKET DELETE command
`GROUPTICKET DELETE;<id>` SHALL delete the group ticket and all its constituent tickets only when its date is strictly later than the system date; otherwise, or for an unknown identifier, it SHALL be rejected leaving every ticket unchanged.

#### Scenario: All-or-nothing deletion
- **WHEN** future group ticket `TG01` with two constituents is deleted
- **THEN** `TG01`, `TG01-1` and `TG01-2` no longer exist

#### Scenario: Current group ticket not deleted
- **WHEN** `TG01` is dated on the system date and `GROUPTICKET DELETE;TG01` is executed
- **THEN** an error is reported and `TG01` and its constituents remain

### Requirement: Group ticket persistence
Group tickets SHALL be persisted after every change and restored at startup with their identifier, date, price, group and constituent tickets linked to the restored users. A historical group ticket SHALL be kept even if its group is later deleted, retaining the group identifier and name.

#### Scenario: Restore group ticket
- **WHEN** `TG01` is created and the application restarts
- **THEN** `TG01` exists with the same price, group and constituents, and `G01` is still locked if `TG01` is current or future

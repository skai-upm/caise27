# group-tickets Specification

## Purpose

Lets a group buy one ticket for all its current members, composed of per-member tickets, with size-based discounts and all-or-nothing behavior.

## Requirements

### Requirement: Create group ticket
`GROUPTICKET CREATE;<groupTicketId>;<groupId>;<yyyy-MM-dd>` SHALL create a group ticket for an existing group with at least one member, a unique identifier, and a valid date not earlier than the system date. It SHALL create one constituent individual ticket per member at that moment, each referencing its user and priced at the current base price, and respond `OK GROUP TICKET CREATED -> <Precio>`. On any failure nothing SHALL be created.

#### Scenario: One constituent per member
- **WHEN** group `G01` has members Ana and Pedro and `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** `TG01` contains exactly two individual tickets, one for Ana and one for Pedro, both dated 2026-10-15

#### Scenario: Composition fixed at purchase
- **WHEN** a member is added to `G01` after `TG01` became historical
- **THEN** `TG01` still contains only the tickets of the members at purchase time

#### Scenario: Failed purchase creates nothing
- **WHEN** the group does not exist, is empty, the identifier is already used, or the date is invalid or in the past
- **THEN** an error is shown and no group ticket or constituent ticket exists afterwards

### Requirement: Constituent ticket identifiers
Constituent tickets SHALL be identified as `<groupTicketId>-<n>` with `n` from 1 to the number of members in membership order. If any such identifier already exists, the purchase SHALL be rejected.

#### Scenario: Generated identifiers
- **WHEN** `TG01` is bought for a group of three members
- **THEN** its constituent tickets are `TG01-1`, `TG01-2`, and `TG01-3`

### Requirement: Group discount
The group ticket price SHALL be the sum of its constituent prices with a discount by member count: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%, rounded half-up to two decimals.

#### Scenario: Discount brackets
- **WHEN** the base price is 40.00 and groups of 2, 3, 6, and 11 members buy group tickets
- **THEN** the reported prices are 80.00, 108.00, 192.00, and 220.00

#### Scenario: Bracket boundaries
- **WHEN** the base price is 40.00 and groups of 5, 6, 10, and 11 members buy group tickets
- **THEN** the reported prices are 180.00, 192.00, 320.00, and 220.00

### Requirement: Active group tickets block member deletion
Each constituent ticket SHALL count as a ticket of its user, so a user with a constituent ticket dated on or after the system date SHALL NOT be deletable.

#### Scenario: Member cannot be deleted
- **WHEN** Ana holds a constituent of `TG01` dated 2026-10-15, the system date is 2026-10-10, and `USER DELETE` is executed for Ana with valid credentials
- **THEN** deletion is rejected

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<groupTicketId>` SHALL delete, in this order, the passes of its constituent tickets, the constituent tickets, and the group ticket only when its date is strictly later than the system date; otherwise, or for an unknown identifier, it SHALL be rejected and nothing SHALL be deleted.

#### Scenario: All-or-nothing deletion
- **WHEN** the system date is 2026-10-10 and `GROUPTICKET DELETE;TG01` is executed for `TG01` dated 2026-10-15
- **THEN** `TG01` and all its constituent tickets are deleted

#### Scenario: Current group ticket not deletable
- **WHEN** the system date equals the date of `TG01` and `GROUPTICKET DELETE;TG01` is executed
- **THEN** an error is shown and `TG01` with all constituents remains

#### Scenario: Cascade without partial state
- **WHEN** future `TG01` has constituents with passes and `GROUPTICKET DELETE;TG01` succeeds
- **THEN** its passes, constituents, and the group ticket are gone; when the deletion is rejected, all of them remain

### Requirement: Group ticket persistence
Group tickets SHALL be persisted with their identifier, date, total price, originating group, and constituent tickets, and restored at startup with the same composition. A group ticket SHALL be retained as history after its date passes, including when its group is later deleted.

#### Scenario: Restart restores composition
- **WHEN** `TG01` was bought for two members and the application restarts
- **THEN** `TG01` has the same price, date, group, and the same two constituent tickets linked to their users

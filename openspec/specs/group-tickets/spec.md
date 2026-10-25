# group-tickets Specification

## Purpose

Lets groups buy discounted group tickets composed of one individual ticket per member at purchase time.

## Requirements

### Requirement: Buy group ticket
`GROUPTICKET CREATE;id;groupId;yyyy-MM-dd` SHALL create a group ticket for an existing, non-empty group and generate one individual ticket for each member at the time of purchase, each priced at the current individual price and identified as `<id>-<member DNI>`. It SHALL answer `OK GROUP TICKET CREATED -> <total>`. The date SHALL NOT be before the system date and all identifiers SHALL be unique.

#### Scenario: Purchase generates member tickets
- **WHEN** G01 has 3 members and `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** TG01 contains 3 individual tickets, one per member, each referencing its user

#### Scenario: Later membership changes do not alter the ticket
- **WHEN** TG01 was bought with 3 members and a fourth user joins G01 after TG01's date has passed
- **THEN** TG01 still contains exactly the 3 original tickets

#### Scenario: Empty or unknown group
- **WHEN** the group has no members or does not exist
- **THEN** an `ERROR` is reported and nothing is created

### Requirement: Group discount
The total SHALL be the sum of the individual prices minus a discount by member count: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %, rounded to two decimals.

#### Scenario: Discount brackets at price 40.00
- **WHEN** group tickets are bought for groups of 2, 3, 5, 6, 10 and 11 members
- **THEN** the totals are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00

### Requirement: Delete future group ticket
`GROUPTICKET DELETE;id` SHALL delete a group ticket and all its internal individual tickets only when its date is strictly later than the system date.

#### Scenario: Future group ticket deleted
- **WHEN** the system date is 2026-10-01 and `GROUPTICKET DELETE;TG01` is processed for a ticket dated 2026-10-15
- **THEN** TG01 and its internal tickets are deleted

#### Scenario: Current group ticket kept
- **WHEN** the system date equals the group ticket date
- **THEN** an `ERROR` is reported and nothing is deleted

### Requirement: Internal tickets protect their users
Internal tickets dated on or after the system date SHALL prevent their users from being deleted, exactly like ordinary individual tickets.

#### Scenario: Member cannot be deleted
- **WHEN** Ana holds an internal ticket of TG01 dated 2026-10-15, the system date is 2026-10-01 and `USER DELETE` is processed for Ana
- **THEN** an `ERROR` is reported and Ana is kept

### Requirement: Group tickets persist
Group tickets SHALL be stored with their identifier, date, group, total price and internal tickets, and restored when the application starts again.

#### Scenario: Restart restores group ticket
- **WHEN** TG01 exists and the application restarts
- **THEN** TG01 exists with the same date, group, total and internal tickets

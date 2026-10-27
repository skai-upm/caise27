# group-tickets Specification

## Purpose

Defines group tickets: joint purchases for all current members of a group, composed of individual tickets with size-based discounts, and the group locking they cause while active.

## Requirements

### Requirement: Group ticket purchase
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing group with at least one member, for a date equal to or later than the current system date, with an identifier unique among all tickets.

#### Scenario: Purchase for a group
- **WHEN** the system date is 2027-04-10, group `G01` has Ana and Pedro, and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** the response is `GROUPTICKET CREATED - Total price: 80.00 EUR` and `TG01` exists, referencing `G01`

#### Scenario: Empty group rejected
- **WHEN** group `G02` has no members and `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Past date, unknown group or duplicate identifier rejected
- **WHEN** the date is before the current date, the group does not exist, or the identifier is already used by any ticket
- **THEN** an error is reported and no ticket is created

### Requirement: Composition of individual tickets
A group ticket SHALL be composed of one new individual ticket per member at purchase time, each referencing its user and identified as `<groupTicketIdentifier>:<dni>`; the composition SHALL NOT change afterwards and the group ticket SHALL NOT keep a separate collection of users.

#### Scenario: Derived identifiers
- **WHEN** `TG01` is bought for a group containing the user with DNI `12345678Z`
- **THEN** `TG01` contains an individual ticket `TG01:12345678Z` for that user

#### Scenario: Composition fixed after purchase
- **WHEN** a member is removed from the group after its group ticket's date has passed
- **THEN** the historical group ticket still contains that member's individual ticket

#### Scenario: Derived identifier conflict prevents creation
- **WHEN** a ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing DNI `12345678Z`
- **THEN** an error is reported and neither `TG01` nor any of its components is created

### Requirement: Group discounts
Each component ticket's price SHALL be the current base price minus a discount by the number of members in the purchase — fewer than 3: 0%; 3 to 5: 10%; 6 to 10: 20%; more than 10: 50% — and the group ticket's total SHALL be the sum of its components.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is 40.00 and a group of 6 users buys a group ticket
- **THEN** each component costs 32.00 and the displayed total is 192.00

#### Scenario: Bracket limits
- **WHEN** group tickets are bought by groups of 2, 3, 5, 10 and 11 members at 40.00
- **THEN** the totals are 80.00, 108.00, 180.00, 320.00 and 220.00

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all its component tickets only when its date is strictly later than the current system date; group tickets for the current day or earlier SHALL be kept as historical records.

#### Scenario: Delete a future group ticket
- **WHEN** the system date is 2027-04-10 and `TG01` is dated 2027-04-15
- **THEN** `GROUPTICKET DELETE;TG01` responds `GROUPTICKET DELETED` and neither `TG01` nor `TG01:12345678Z` exists

#### Scenario: Today's group ticket kept
- **WHEN** the system date equals the group ticket's date
- **THEN** `GROUPTICKET DELETE` reports an error and the ticket remains

### Requirement: Group locked by active group tickets
A group SHALL be locked while at least one associated group ticket is dated on or after the current system date; the locked state SHALL be derived from the tickets and the system date, never stored.

#### Scenario: Lock released after the date passes
- **WHEN** `G01` has group ticket `TG01` for 2027-04-15 and the system date moves to 2027-04-16
- **THEN** `G01` is no longer locked

#### Scenario: Lock released after deleting the future ticket
- **WHEN** `G01`'s only active group ticket is future-dated and is deleted with GROUPTICKET DELETE
- **THEN** `G01` can be modified again

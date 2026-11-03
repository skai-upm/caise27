# Spec Delta

## Purpose

Lets a nonempty group buy a discounted group ticket composed of one individual ticket per current member, created atomically, and delete it while it is still in the future.

## ADDED Requirements

### Requirement: Group ticket composition
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing nonempty group and a date on or after the current system date, create a group ticket composed of one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>`, each referencing its user; the composition SHALL NOT change afterwards.

#### Scenario: Composition identifiers
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` runs for a group containing the user with DNI 12345678Z
- **THEN** the group ticket contains an individual ticket `TG01:12345678Z` for that user

#### Scenario: Empty group
- **WHEN** a group ticket is requested for an empty group
- **THEN** creation is rejected

#### Scenario: Composition is fixed
- **WHEN** a member is removed from the group after its group ticket becomes historical
- **THEN** the historical group ticket still contains that member's ticket

### Requirement: Group discounts
Each composed ticket SHALL cost the current base price minus a discount by member count: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %; the total, the sum of the composed tickets, SHALL be displayed.

#### Scenario: Discount brackets
- **WHEN** groups of 2, 3, 6 and 11 members buy group tickets at a base price of 40
- **THEN** the displayed totals are 80.00, 108.00, 192.00 and 220.00

### Requirement: Atomic group purchase
If the group ticket identifier or any derived composition identifier conflicts with an existing ticket, the purchase SHALL be rejected and neither the group ticket nor any composed ticket SHALL be created.

#### Scenario: Derived identifier conflict
- **WHEN** a ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;...` includes that user
- **THEN** no group ticket and no composed ticket are created

### Requirement: Group ticket references its group
A group ticket SHALL reference the purchasing group while it exists; the group SHALL NOT reference its tickets, and the reference MAY become null when the group is deleted.

#### Scenario: Group deleted
- **WHEN** the purchasing group is deleted after the ticket became historical
- **THEN** the group ticket is retained without a group reference

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the current system date, together with its composed tickets; group tickets for today or earlier SHALL be retained.

#### Scenario: Delete future group ticket
- **WHEN** a future group ticket is deleted
- **THEN** it and all its composed tickets are removed

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE` targets a group ticket dated today
- **THEN** deletion is rejected

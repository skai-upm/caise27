# group-ticket-purchasing Specification

## Purpose

Defines group ticket purchase as an all-or-nothing composition of individual tickets with member-count discounts, and group ticket deletion (F2-C04).

## Requirements

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create, for an existing non-empty group and a date equal to or later than the current system date, a group ticket composed of one new individual ticket per member at purchase time, each with identifier `<groupTicketIdentifier>:<dni>` and referencing its user. The group ticket SHALL reference the purchasing group and SHALL NOT keep a separate user collection. Its composition SHALL NOT change afterwards.

#### Scenario: Composition created
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed and G01 contains Ana (DNI 12345678Z) and Pedro (DNI 87654321X)
- **THEN** TG01 is created with individual tickets `TG01:12345678Z` and `TG01:87654321X`

#### Scenario: Empty group rejected
- **WHEN** a group ticket is requested for a group with no members
- **THEN** an error is reported and nothing is created

#### Scenario: Past date or unknown group rejected
- **WHEN** a group ticket is requested for a date before the current date or for a nonexistent group
- **THEN** an error is reported and nothing is created

#### Scenario: Composition fixed after purchase
- **WHEN** a member is removed from the group after the group ticket's date has passed
- **THEN** the historical group ticket still contains that member's individual ticket

### Requirement: Atomic identifier check
The group ticket identifier and every derived constituent identifier SHALL be checked against the shared ticket identifier space before anything is created; any conflict SHALL reject the whole purchase.

#### Scenario: Derived identifier conflict
- **WHEN** a standalone ticket `TG01:12345678Z` exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing that DNI
- **THEN** an error is reported and neither TG01 nor any constituent ticket is created

### Requirement: Group discount
Each constituent ticket price SHALL be the current base price with a discount by member count at purchase: fewer than 3 members 0%, 3 to 5 members 10%, 6 to 10 members 20%, more than 10 members 50%. The group ticket total SHALL be the sum of its constituent prices and SHALL be displayed on success.

#### Scenario: Discount brackets at base price 40
- **WHEN** group tickets are purchased for groups of 2, 3, 6 and 11 members at base price 40.00
- **THEN** the displayed totals are 80.00, 108.00, 192.00 and 220.00 respectively

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all individual tickets of its composition only when its date is strictly later than the current system date. Group tickets for the current day or earlier SHALL be retained as historical records. From Delivery 3 a valid deletion SHALL also delete all passes associated with the constituent tickets, as one operation without partial deletions.

#### Scenario: Future group ticket deleted
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated after the current date
- **THEN** TG01 and its constituent tickets no longer exist

#### Scenario: Group ticket for today retained
- **WHEN** `GROUPTICKET DELETE` is processed for a group ticket dated on the current date
- **THEN** an error is reported and the group ticket is retained

#### Scenario: Group deletion cascades to passes
- **WHEN** a future group ticket whose constituents hold passes is deleted
- **THEN** those passes, the constituents and the group ticket no longer exist
